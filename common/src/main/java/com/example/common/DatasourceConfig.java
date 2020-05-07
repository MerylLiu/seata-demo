package com.example.common;

import com.alibaba.druid.pool.DruidDataSource;
import com.extm.SqlSessionFactoryBeanEx;
import com.jds.core.dao.BaseDao;
import io.seata.rm.datasource.DataSourceProxy;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

/**
 * @Author : Meryl
 * @Description:
 * @Date: Created in 2019/8/28
 * @Modify by :
 */
@Configuration
public class DatasourceConfig {
    @javax.annotation.Resource
    BaseDao baseDao;

    @Conditional(DistributedTransCondition.class)
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource druidDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        return druidDataSource;
    }

    @Primary
    @Conditional(DistributedTransCondition.class)
    @Bean("dataSource")
    public DataSourceProxy dataSource(DataSource druidDataSource) {
        return new DataSourceProxy(druidDataSource);
    }

    @Conditional(DistributedTransCondition.class)
    @Primary
    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactoryBean sqlSessionFactory(DataSourceProxy dataSource) {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBeanEx();
        sqlSessionFactoryBean.setDataSource(dataSource);

        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] mybatisConfigXml = resolver.getResources("classpath*:com/**/cfg/mybatis.cfg.xml");
            sqlSessionFactoryBean.setConfigLocation(mybatisConfigXml[0]);

            Resource[] mybatisMapperXml = resolver.getResources("classpath*:com/**/sql/*.xml");
            sqlSessionFactoryBean.setMapperLocations(mybatisMapperXml);

            SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactoryBean.getObject());
            baseDao.setSqlSessionTemplate(sqlSessionTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sqlSessionFactoryBean;
    }
}
