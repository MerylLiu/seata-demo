package com.example.common.config;

import com.alibaba.fastjson.JSON;
import com.example.common.seata.SeataTransactionalTemplate;
import io.seata.common.exception.ShouldNeverHappenException;
import io.seata.config.ConfigurationChangeEvent;
import io.seata.config.ConfigurationChangeListener;
import io.seata.config.ConfigurationFactory;
import io.seata.rm.GlobalLockTemplate;
import io.seata.tm.api.DefaultFailureHandlerImpl;
import io.seata.tm.api.FailureHandler;
import io.seata.tm.api.TransactionalExecutor;
import io.seata.tm.api.transaction.NoRollbackRule;
import io.seata.tm.api.transaction.RollbackRule;
import io.seata.tm.api.transaction.TransactionInfo;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

public class DistTransactionInterceptor implements ConfigurationChangeListener, MethodInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistTransactionInterceptor.class);
    private static final FailureHandler DEFAULT_FAIL_HANDLER = new DefaultFailureHandlerImpl();
    private final SeataTransactionalTemplate transactionalTemplate = new SeataTransactionalTemplate();
    private final GlobalLockTemplate<Object> globalLockTemplate = new GlobalLockTemplate();
    private final FailureHandler failureHandler;
    private volatile boolean disable;

    int timeoutMills = 60000;
    Propagation propagation = Propagation.REQUIRED;
    Class<? extends Throwable>[] rollbackFor = new Class[]{};
    String[] rollbackForClassName = new String[]{};
    Class<? extends Throwable>[] noRollbackFor = new Class[]{};
    String[] noRollbackForClassName = new String[]{};


    public DistTransactionInterceptor(FailureHandler failureHandler) {
        this.failureHandler = failureHandler == null ? DEFAULT_FAIL_HANDLER : failureHandler;
        this.disable = ConfigurationFactory.getInstance().getBoolean("service.disableGlobalTransaction", false);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Class<?> targetClass = methodInvocation.getThis() != null ? AopUtils.getTargetClass(methodInvocation.getThis()) : null;
        Method specificMethod = ClassUtils.getMostSpecificMethod(methodInvocation.getMethod(), targetClass);
        Method method = BridgeMethodResolver.findBridgedMethod(specificMethod);

        if (!this.disable && method.getName().startsWith("tx")) {
            return this.handleGlobalTransaction(methodInvocation);
        } else {
            return !this.disable ? this.handleGlobalLock(methodInvocation) : methodInvocation.proceed();
        }
    }

    private Object handleGlobalLock(MethodInvocation methodInvocation) throws Exception {
        return this.globalLockTemplate.execute(() -> {
            try {
                return methodInvocation.proceed();
            } catch (Exception var2) {
                throw var2;
            } catch (Throwable var3) {
                throw new RuntimeException(var3);
            }
        });
    }

    private Object handleGlobalTransaction(final MethodInvocation methodInvocation) throws Throwable {
        try {
            Object res = this.transactionalTemplate.execute(new TransactionalExecutor() {
                public Object execute() throws Throwable {
                    return methodInvocation.proceed();
                }

                public String name() {
                    return DistTransactionInterceptor.this.formatMethod(methodInvocation.getMethod());
                }

                public TransactionInfo getTransactionInfo() {
                    TransactionInfo transactionInfo = new TransactionInfo();
                    transactionInfo.setTimeOut(timeoutMills);
                    transactionInfo.setName(this.name());
                    Set<RollbackRule> rollbackRules = new LinkedHashSet();
                    Class[] var3 = rollbackFor;
                    int var4 = var3.length;

                    int var5;
                    Class rbRule;
                    for (var5 = 0; var5 < var4; ++var5) {
                        rbRule = var3[var5];
                        rollbackRules.add(new RollbackRule(rbRule));
                    }

                    String[] var7 = rollbackForClassName;
                    var4 = var7.length;

                    String rbRulex;
                    for (var5 = 0; var5 < var4; ++var5) {
                        rbRulex = var7[var5];
                        rollbackRules.add(new RollbackRule(rbRulex));
                    }

                    var3 = noRollbackFor;
                    var4 = var3.length;

                    for (var5 = 0; var5 < var4; ++var5) {
                        rbRule = var3[var5];
                        rollbackRules.add(new NoRollbackRule(rbRule));
                    }

                    var7 = noRollbackForClassName;
                    var4 = var7.length;

                    for (var5 = 0; var5 < var4; ++var5) {
                        rbRulex = var7[var5];
                        rollbackRules.add(new NoRollbackRule(rbRulex));
                    }

                    transactionInfo.setRollbackRules(rollbackRules);
                    return transactionInfo;
                }
            });

            return res;
        } catch (TransactionalExecutor.ExecutionException var5) {
            TransactionalExecutor.Code code = var5.getCode();
            switch (code) {
                case RollbackDone:
                    throw var5.getOriginalException();
                case BeginFailure:
                    this.failureHandler.onBeginFailure(var5.getTransaction(), var5.getCause());
                    throw var5.getCause();
                case CommitFailure:
                    this.failureHandler.onCommitFailure(var5.getTransaction(), var5.getCause());
                    throw var5.getCause();
                case RollbackFailure:
                    this.failureHandler.onRollbackFailure(var5.getTransaction(), var5.getCause());
                    throw var5.getCause();
                default:
                    throw new ShouldNeverHappenException(String.format("Unknown TransactionalExecutor.Code: %s", code));
            }
        }
    }

    private String formatMethod(Method method) {
        StringBuilder sb = (new StringBuilder(method.getName())).append("(");
        Class<?>[] params = method.getParameterTypes();
        int in = 0;
        Class[] var5 = params;
        int var6 = params.length;

        for (int var7 = 0; var7 < var6; ++var7) {
            Class<?> clazz = var5[var7];
            sb.append(clazz.getName());
            ++in;
            if (in < params.length) {
                sb.append(", ");
            }
        }

        return sb.append(")").toString();
    }

    @Override
    public void onChangeEvent(ConfigurationChangeEvent event) {
        if ("service.disableGlobalTransaction".equals(event.getDataId())) {
//            LOGGER.info("{} config changed, old value:{}, new value:{}", new Object[]{"service.disableGlobalTransaction", this.disable, event.getNewValue()});
//            this.disable = Boolean.parseBoolean(event.getNewValue().trim());
        }
    }
}
