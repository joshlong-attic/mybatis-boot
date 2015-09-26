package org.springframework.boot.autoconfigure.orm.mybatis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

/**
 * Configures the MyBatis-Spring integration. For more control, you may use {@link MapperScan}. By default,
 * this will look for all mapper interfaces in or below the current auto-configuration package (which is implied by
 * Spring's {@link org.springframework.context.annotation.ComponentScan}.)
 *
 * @author Josh Long
 */
@Configuration
@ConditionalOnClass({MapperScan.class, SqlSessionFactory.class})
@EnableConfigurationProperties(MyBatisProperties.class)
public class MyBatisAutoConfiguration implements ResourceLoaderAware {

    private static Log log = LogFactory.getLog(MyBatisAutoConfiguration.class);

    private ResourceLoader resourceLoader;

    @Autowired(required = false)
    private Interceptor[] plugins;

    @Autowired(required = false)
    private DatabaseIdProvider[] databaseIdProviders;

    @Autowired(required = false)
    private MyBatisProperties properties;

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);

        if (properties.getConfigLocation() != null) {
            sqlSessionFactoryBean.setConfigLocation(properties.getConfigLocation());
        }

        if (properties.getMapperLocations() != null && properties.getMapperLocations().length > 0) {
            sqlSessionFactoryBean.setMapperLocations(properties.getMapperLocations());
        }

        if (null != plugins && plugins.length > 0) {
            sqlSessionFactoryBean.setPlugins(plugins);
        }

        DatabaseIdProvider[] zeroOrOneDbIdProviders = this.databaseIdProviders;
        if (null != zeroOrOneDbIdProviders && zeroOrOneDbIdProviders.length > 0) {
            Assert.isTrue(zeroOrOneDbIdProviders.length == 1,
                    "there is more than one " + DatabaseIdProvider.class.getName() + " bean defined.");
            sqlSessionFactoryBean.setDatabaseIdProvider(zeroOrOneDbIdProviders[0]);
        }

        return sqlSessionFactoryBean;
    }

    @Bean
    @ConditionalOnMissingBean(SqlSessionTemplate.class)
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    static class AutoConfiguredMapperScannerRegistrar implements
            BeanFactoryAware,
            ImportBeanDefinitionRegistrar,
            ResourceLoaderAware {

        private BeanFactory beanFactory;

        private ResourceLoader resourceLoader;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {

            ClassPathMapperScanner scanner = new ClassPathMapperScanner(registry);

            List<String> pkgs = AutoConfigurationPackages.get(this.beanFactory);
            for (String pkg : pkgs) {
                log.debug("found MyBatis auto-configuration package '" + pkg +"'");
            }

            if (this.resourceLoader != null) {
                scanner.setResourceLoader(this.resourceLoader);
            }

            scanner.registerFilters();
            scanner.doScan(pkgs.toArray(new String[pkgs.size()]));
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }

        @Override
        public void setResourceLoader(ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
        }
    }

    @Configuration
    @Import({AutoConfiguredMapperScannerRegistrar.class})
    @ConditionalOnMissingBean(MapperFactoryBean.class)  /// @MapperScanner brings this in..
    static class MapperScannerRegistrarNotFoundConfiguration {

        @PostConstruct
        public void after() {
            log.debug("NOT FOUND: " + MapperFactoryBean.class.getName());
        }
    }

    @Configuration
    @ConditionalOnBean(MapperFactoryBean.class)
    static class MapperScannerRegistrarFoundConfiguration {

        @PostConstruct
        public void after() {
            log.debug("FOUND: " + MapperFactoryBean.class.getName());
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}

