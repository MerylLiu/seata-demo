package com.example.common.seata;

import io.seata.common.util.StringUtils;
import io.seata.config.AbstractConfiguration;
import io.seata.config.ConfigurationChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SeataConfiguration extends AbstractConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(SeataConfiguration.class);

    private static final String CONFIG_TYPE = "SeataConfig";
    private static final String PREFIX = "seata.";

    private static volatile SeataConfiguration instance;
    private static final ConcurrentMap<String, Set<ConfigurationChangeListener>> LISTENER_SERVICE_MAP = new ConcurrentHashMap<>();

    public static SeataConfiguration getInstance() {
        if (null == instance) {
            synchronized (SeataConfiguration.class) {
                if (null == instance) {
                    instance = new SeataConfiguration();
                }
            }
        }
        return instance;
    }

    private SeataConfiguration() {
    }

    private static final char LINE = '-';


    @Override
    public String getTypeName() {
        return CONFIG_TYPE;
    }

    @Override
    public String getConfig(String dataId, String defaultValue, long timeoutMills) {
        dataId = dataId.startsWith(PREFIX) ? dataId : PREFIX.concat(formatCamel(dataId, LINE));
        String value;
        // 从系统变量获取
        if ((value = getConfigFromSysPro(dataId)) != null) {
            return value;
        }

        // 从配置文件获取
        ApplicationContext applicationContext = SpringUtils.getApplicationContext();
        if (null == applicationContext) {
            return defaultValue;
        }
        value = applicationContext.getEnvironment().getProperty(dataId);

        if (null == value) {
            logger.error("properties null [{}] ", dataId);
            return defaultValue;
        }

        return value;
    }

    private String formatCamel(String param, char sign) {
        if (ObjectUtils.isEmpty(param)) {
            return StringUtils.EMPTY;
        }
        int len = param.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = param.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(sign);
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean putConfig(String s, String s1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean putConfigIfAbsent(String s, String s1, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeConfig(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (null == dataId || null == listener) {
            return;
        }
        LISTENER_SERVICE_MAP.putIfAbsent(dataId, ConcurrentHashMap.newKeySet());
        LISTENER_SERVICE_MAP.get(dataId).add(listener);
    }

    @Override
    public void removeConfigListener(String dataId, ConfigurationChangeListener listener) {
        if (!LISTENER_SERVICE_MAP.containsKey(dataId) || listener == null) {
            return;
        }
        LISTENER_SERVICE_MAP.get(dataId).remove(listener);
    }

    @Override
    public Set<ConfigurationChangeListener> getConfigListeners(String dataId) {
        return LISTENER_SERVICE_MAP.get(dataId);
    }
}
