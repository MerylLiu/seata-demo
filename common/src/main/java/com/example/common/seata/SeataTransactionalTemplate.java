package com.example.common.seata;

import io.seata.common.exception.ShouldNeverHappenException;
import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.core.model.GlobalStatus;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import io.seata.tm.api.TransactionalExecutor;
import io.seata.tm.api.TransactionalExecutor.Code;
import io.seata.tm.api.TransactionalExecutor.ExecutionException;
import io.seata.tm.api.transaction.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class SeataTransactionalTemplate {
    private static final Logger LOGGER = LoggerFactory.getLogger(io.seata.tm.api.TransactionalTemplate.class);

    public Object execute(TransactionalExecutor business) throws Throwable {
        TransactionInfo txInfo = business.getTransactionInfo();
        if (txInfo == null) {
            throw new ShouldNeverHappenException("transactionInfo does not exist");
        } else {
            GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
            Propagation propagation = txInfo.getPropagation();
            SuspendedResourcesHolder suspendedResourcesHolder = null;

            Object ex;
            try {
                Object rs;
                switch (propagation) {
                    case NOT_SUPPORTED:
                        suspendedResourcesHolder = tx.suspend(true);
                        rs = business.execute();
                        return rs;
                    case REQUIRES_NEW:
                        suspendedResourcesHolder = tx.suspend(true);
                        break;
                    case SUPPORTS:
                        if (!this.existingTransaction()) {
                            rs = business.execute();
                            return rs;
                        }
                    case REQUIRED:
                        break;
                    case NEVER:
                        if (this.existingTransaction()) {
                            throw new TransactionException(String.format("Existing transaction found for transaction marked with propagation 'never',xid = %s", RootContext.getXID()));
                        }

                        rs = business.execute();
                        return rs;
                    case MANDATORY:
                        if (!this.existingTransaction()) {
                            throw new TransactionException("No existing transaction found for transaction marked with propagation 'mandatory'");
                        }
                        break;
                    default:
                        throw new TransactionException("Not Supported Propagation:" + propagation);
                }

                try {
                    this.beginTransaction(txInfo, tx);
                    rs = null;

                    try {
                        rs = business.execute();
                    } catch (Throwable var16) {
                        ex = var16;
                        this.completeTransactionAfterThrowing(txInfo, tx, var16);
                        throw var16;
                    }

                    this.commitTransaction(tx);
                    ex = rs;
                } finally {
                    this.triggerAfterCompletion();
                    this.cleanUp();
                }
            } finally {
                tx.resume(suspendedResourcesHolder);
            }

            return ex;
        }
    }

    public boolean existingTransaction() {
        return StringUtils.isNotEmpty(RootContext.getXID());
    }

    private void completeTransactionAfterThrowing(TransactionInfo txInfo, GlobalTransaction tx, Throwable ex) throws ExecutionException {
        if (txInfo != null && txInfo.rollbackOn(ex)) {
            try {
                this.rollbackTransaction(tx, ex);
            } catch (TransactionException var5) {
                throw new ExecutionException(tx, var5, Code.RollbackFailure, ex);
            }
        } else {
            this.commitTransaction(tx);
        }

    }

    private void commitTransaction(GlobalTransaction tx) throws ExecutionException {
        try {
            this.triggerBeforeCommit();
            tx.commit();
            this.triggerAfterCommit();
        } catch (TransactionException var3) {
            throw new ExecutionException(tx, var3, Code.CommitFailure);
        }
    }

    private void rollbackTransaction(GlobalTransaction tx, Throwable ex) throws TransactionException, ExecutionException {
        this.triggerBeforeRollback();
        tx.rollback();
        this.triggerAfterRollback();
        throw new ExecutionException(tx, GlobalStatus.RollbackRetrying.equals(tx.getLocalStatus()) ? Code.RollbackRetrying : Code.RollbackDone, ex);
    }

    private void beginTransaction(TransactionInfo txInfo, GlobalTransaction tx) throws ExecutionException {
        try {
            this.triggerBeforeBegin();
            tx.begin(txInfo.getTimeOut(), txInfo.getName());
            this.triggerAfterBegin();
        } catch (TransactionException var4) {
            throw new ExecutionException(tx, var4, Code.BeginFailure);
        }
    }

    private void triggerBeforeBegin() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.beforeBegin();
            } catch (Exception var4) {
                LOGGER.error("Failed execute beforeBegin in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerAfterBegin() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.afterBegin();
            } catch (Exception var4) {
                LOGGER.error("Failed execute afterBegin in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerBeforeRollback() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.beforeRollback();
            } catch (Exception var4) {
                LOGGER.error("Failed execute beforeRollback in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerAfterRollback() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.afterRollback();
            } catch (Exception var4) {
                LOGGER.error("Failed execute afterRollback in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerBeforeCommit() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.beforeCommit();
            } catch (Exception var4) {
                LOGGER.error("Failed execute beforeCommit in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerAfterCommit() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.afterCommit();
            } catch (Exception var4) {
                LOGGER.error("Failed execute afterCommit in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void triggerAfterCompletion() {
        Iterator var1 = this.getCurrentHooks().iterator();

        while (var1.hasNext()) {
            TransactionHook hook = (TransactionHook) var1.next();

            try {
                hook.afterCompletion();
            } catch (Exception var4) {
                LOGGER.error("Failed execute afterCompletion in hook {}", var4.getMessage(), var4);
            }
        }

    }

    private void cleanUp() {
        TransactionHookManager.clear();
    }

    private List<TransactionHook> getCurrentHooks() {
        return TransactionHookManager.getHooks();
    }
}
