package demo.app

import demo.NineDataSourceConnectionSourceFactory
import demo.NineHibernateConnectionSourceFactory
import demo.NineHibernateDatastoreConnectionSourcesRegistrar
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import grails.core.GrailsClass
import grails.orm.bootstrap.HibernateDatastoreSpringInitializer
import grails.util.Holders
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.gorm.events.ConfigurableApplicationContextEventPublisher
import org.grails.datastore.gorm.events.DefaultApplicationEventPublisher
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.connections.HibernateConnectionSourceFactory
import org.grails.orm.hibernate.support.HibernateDatastoreConnectionSourcesRegistrar
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.PropertyResolver
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

@CompileStatic
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    @CompileDynamic
    Closure doWithSpring() {

        def grailsApp = Holders.grailsApplication
        def conf = grailsApp.config
        ApplicationContext ctx = grailsApp.mainContext
        def domainClasses = grailsApp.getArtefacts(DomainClassArtefactHandler.TYPE).collect() { GrailsClass cls -> cls.clazz }
        return { ->
            dataSourceConnectionSourceFactory(NineDataSourceConnectionSourceFactory)
            hibernateDatastoreConnectionSourcesRegistrar(NineHibernateDatastoreConnectionSourcesRegistrar)
            hibernateConnectionSourceFactory(NineHibernateConnectionSourceFactory, domainClasses as Class[]) { bean ->
                bean.autowire = true
                dataSourceConnectionSourceFactory = ref('dataSourceConnectionSourceFactory')
            }
            //Need to override here, just because the original definition takes `hibernateConnectionSourceFactory` as constructor args
            //and unless overridden here, it keeps pointing to old `dataSourceConnectionSourceFactory` from hibernate plugin
            hibernateDatastore(HibernateDatastore, (PropertyResolver)conf, hibernateConnectionSourceFactory, findEventPublisher(ctx)) { bean->
                bean.primary = true
            }
        }
    }

    def findEventPublisher(BeanDefinitionRegistry beanDefinitionRegistry) {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver()
            ApplicationEventPublisher eventPublisher
            if(beanDefinitionRegistry instanceof ConfigurableApplicationContext){
                eventPublisher = new ConfigurableApplicationContextEventPublisher((ConfigurableApplicationContext)beanDefinitionRegistry)
            }
            else if(resourcePatternResolver.resourceLoader instanceof ConfigurableApplicationContext) {
                eventPublisher = new ConfigurableApplicationContextEventPublisher((ConfigurableApplicationContext)resourcePatternResolver.resourceLoader)
            }
            else {
                eventPublisher = new DefaultApplicationEventPublisher()
            }
            return eventPublisher
        }
}