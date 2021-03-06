package grails.plugin.hibernate

import grails.config.Config
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.orm.bootstrap.HibernateDatastoreSpringInitializer
import grails.plugins.Plugin
import grails.validation.ConstrainedProperty
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.orm.hibernate.support.AbstractMultipleDataSourceAggregatePersistenceContextInterceptor
import org.grails.orm.hibernate.validation.UniqueConstraint
import org.springframework.beans.factory.support.BeanDefinitionRegistry
/**
 * Plugin that integrates Hibernate into a Grails application
 *
 * @author Graeme Rocher
 * @since 3.0
 */
@CompileStatic
class HibernateGrailsPlugin extends Plugin {

    public static final String DEFAULT_DATA_SOURCE_NAME = HibernateDatastoreSpringInitializer.DEFAULT_DATA_SOURCE_NAME

    def grailsVersion = '3.0.0 > *'

    def author = 'Grails Core Team'
    def title = 'Hibernate 4 for Grails'
    def description = 'Provides integration between Grails and Hibernate 4 through GORM'
    def documentation = 'http://grails.org/plugin/hibernate4'

    def observe = ['domainClass']
    def loadAfter = ['controllers', 'domainClass']
    def watchedResources = ['file:./grails-app/conf/hibernate/**.xml']
    def pluginExcludes = ['src/templates/**']

    def license = 'APACHE'
    def organization = [name: 'Grails', url: 'http://grails.org']
    def issueManagement = [system: 'JIRA', url: 'http://jira.grails.org/browse/GPHIB']
    def scm = [url: 'https://github.com/grails-plugins/grails-hibernate4-plugin']

    Set<String> dataSourceNames

    Closure doWithSpring() {{->
        GrailsApplication grailsApplication = grailsApplication
        Config config = grailsApplication.config
        dataSourceNames = AbstractMultipleDataSourceAggregatePersistenceContextInterceptor.calculateDataSourceNames(grailsApplication)


        def springInitializer = new HibernateDatastoreSpringInitializer(config, grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE).collect() { GrailsClass cls -> cls.clazz })
        springInitializer.registerApplicationIfNotPresent = false
        springInitializer.dataSources = dataSourceNames
        def beans = springInitializer.getBeanDefinitions((BeanDefinitionRegistry)applicationContext)

        beans.delegate = delegate
        beans.call()
    }}

    @Override
    void onShutdown(Map<String, Object> event) {
        ConstrainedProperty.removeConstraint(UniqueConstraint.UNIQUE_CONSTRAINT, UniqueConstraint)
    }

    /*@Override
    void onChange(Map<String, Object> event) {

        if(event.source instanceof Class) {
            Class cls = (Class)event.source
            GrailsDomainClass dc = (GrailsDomainClass)grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, cls.name)

            if(!dc || !GrailsHibernateUtil.isMappedWithHibernate(dc)) {
                return
            }

            GrailsDomainBinder.clearMappingCache(cls)

            ApplicationContext applicationContext = applicationContext
            for(String dataSourceName in dataSourceNames) {
                boolean isDefault = dataSourceName == GrailsDomainClassProperty.DEFAULT_DATA_SOURCE
                String suffix = isDefault ? '' : '_' + dataSourceName
                String sessionFactoryName = isDefault ? HibernateDatastoreSpringInitializer.SESSION_FACTORY_BEAN_NAME : "sessionFactory$suffix"

                if(applicationContext instanceof BeanDefinitionRegistry) {
                    BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) applicationContext

                    def holder = applicationContext.getBean("${SessionFactoryHolder.BEAN_ID}${suffix}", SessionFactoryHolder)
                    holder.sessionFactory.close()

                    def sessionFactoryBeanDefinition = beanDefinitionRegistry.getBeanDefinition(sessionFactoryName)
                    sessionFactoryBeanDefinition.propertyValues.add("proxyIfReloadEnabled", false)
                    applicationContext.registerBeanDefinition("\$${sessionFactoryName}", sessionFactoryBeanDefinition)

                    def newSessionFactory = applicationContext.getBean("\$${sessionFactoryName}", SessionFactory)

                    holder.setSessionFactory(
                            newSessionFactory
                    )
                }
            }

            def postInit = new HibernateDatastoreSpringInitializer.PostInitializationHandling()
            postInit.applicationContext = applicationContext
            postInit.grailsApplication = grailsApplication
            postInit.afterPropertiesSet()
        }
    }*/
}
