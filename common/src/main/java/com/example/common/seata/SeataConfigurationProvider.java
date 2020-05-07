package com.example.common.seata;

import io.seata.common.loader.LoadLevel;
import io.seata.config.Configuration;
import io.seata.config.ConfigurationProvider;
import io.seata.config.ExtConfigurationProvider;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

@LoadLevel(name = "SeataConfig", order = 1)
public class SeataConfigurationProvider implements ExtConfigurationProvider, ConfigurationProvider {
    private static final String GET = "get";
    private static final String PREFIX = "seata.";

    @Override
    public Configuration provide(Configuration originalConfiguration) {
        ApplicationContext applicationContext = SpringUtils.getApplicationContext();

        return (Configuration) Enhancer.create(originalConfiguration.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy)
                    throws Throwable {

                if (method.getName().startsWith(GET) && args.length > 0) {
                    Object result = null;
                    String rawDataId = (String) args[0];
                    if (args.length == 1) {
                        result = applicationContext.getEnvironment().getProperty(PREFIX.concat(rawDataId));
                    }
                    if (null != result) {
                        return result;
                    }
                }

                return method.invoke(originalConfiguration, args);
            }
        });
    }

    @Override
    public Configuration provide() {
        return SeataConfiguration.getInstance();
    }
}
