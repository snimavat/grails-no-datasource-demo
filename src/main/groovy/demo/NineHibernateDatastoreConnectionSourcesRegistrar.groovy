package demo

import groovy.transform.CompileStatic
import org.grails.datastore.gorm.bootstrap.support.InstanceFactoryBean
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.hibernate.SessionFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.config.ConstructorArgumentValues
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.core.Ordered
import org.springframework.transaction.PlatformTransactionManager

import javax.sql.DataSource

@CompileStatic
class NineHibernateDatastoreConnectionSourcesRegistrar implements BeanDefinitionRegistryPostProcessor, Ordered {

    @Override
    void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        //TODO, this will need to come from config
        List<String> dataSourceNames = ["dataSource"]

        for(String dataSourceName in dataSourceNames) {
            boolean isDefault = dataSourceName == ConnectionSource.DEFAULT || dataSourceName == Settings.SETTING_DATASOURCE
            String dataSourceBeanName = isDefault ? Settings.SETTING_DATASOURCE : "${Settings.SETTING_DATASOURCE}_$dataSourceName"

            if(!registry.containsBeanDefinition(dataSourceBeanName)) {
                def dataSourceBean = new RootBeanDefinition()
                dataSourceBean.setTargetType(DataSource)
                dataSourceBean.setBeanClass(InstanceFactoryBean)
                def args = new ConstructorArgumentValues()
                String spel = "#{dataSourceConnectionSourceFactory.create('$dataSourceName', environment).source}".toString()
                args.addGenericArgumentValue(spel)
                dataSourceBean.setConstructorArgumentValues(
                        args
                )
                registry.registerBeanDefinition(dataSourceBeanName, dataSourceBean)
            }

            if(!isDefault) {
                String suffix = '_' + dataSourceName
                String sessionFactoryName = "sessionFactory$suffix"
                String transactionManagerBeanName = "transactionManager$suffix"

                def sessionFactoryBean = new RootBeanDefinition()
                sessionFactoryBean.setTargetType(SessionFactory)
                sessionFactoryBean.setBeanClass(InstanceFactoryBean)
                def args = new ConstructorArgumentValues()
                args.addGenericArgumentValue("#{hibernateDatastore.getDatastoreForConnection('$dataSourceName').sessionFactory}".toString())
                sessionFactoryBean.setConstructorArgumentValues(
                        args
                )
                registry.registerBeanDefinition(
                        sessionFactoryName,
                        sessionFactoryBean
                )

                def transactionManagerBean = new RootBeanDefinition()
                transactionManagerBean.setTargetType(PlatformTransactionManager)
                transactionManagerBean.setBeanClass(InstanceFactoryBean)
                def txMgrArgs = new ConstructorArgumentValues()
                txMgrArgs.addGenericArgumentValue("#{hibernateDatastore.getDatastoreForConnection('$dataSourceName').transactionManager}".toString())
                transactionManagerBean.setConstructorArgumentValues(
                        txMgrArgs
                )
                registry.registerBeanDefinition(
                        transactionManagerBeanName,
                        transactionManagerBean
                )
            }
        }
    }

    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // no-op
    }

    @Override
    int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100
    }
}
