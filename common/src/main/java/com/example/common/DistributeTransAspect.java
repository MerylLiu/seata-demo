package com.example.common;

import io.seata.common.util.StringUtils;
import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DistributeTransAspect {
    private final static Logger logger = LoggerFactory.getLogger(DistributeTransAspect.class);
    private static final String AOP_POINTCUT_EXPRESSION = "execution(* com..*.service..*.*(..))";

    @Before(AOP_POINTCUT_EXPRESSION)
    public void before(JoinPoint joinPoint) throws Exception {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        boolean enable = DistributedTransImportSelector.enabledTransaction;
        if (method.getName().startsWith("tx") && enable) {
            logger.info("拦截到需要分布式事务的方法," + method.getName());
            // 此处可用redis或者定时任务来获取一个key判断是否需要关闭分布式事务
            GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();
            tx.begin();
            logger.info("创建分布式事务完毕" + tx.getXid());
        }
    }

    @AfterThrowing(throwing = "e", pointcut = AOP_POINTCUT_EXPRESSION)
    public void doRecoveryActions(Throwable e) throws Exception {
        logger.info("方法执行异常:{}", e.getMessage());
        String xid = RootContext.getXID();
        boolean enable = DistributedTransImportSelector.enabledTransaction;
        if (!StringUtils.isBlank(xid) && enable) {
            GlobalTransactionContext.reload(xid).rollback();
        }
    }

    @AfterReturning(returning = "result", pointcut = AOP_POINTCUT_EXPRESSION)
    public void afterReturning(JoinPoint point, Object result) throws Exception {
        logger.info("方法执行结束:{}", result);
        String xid = RootContext.getXID();
        boolean enable = DistributedTransImportSelector.enabledTransaction;
        if (!StringUtils.isBlank(xid) && enable) {
            logger.info("分布式事务Id:{}", xid);
            GlobalTransactionContext.reload(xid).commit();
        }
    }
}
