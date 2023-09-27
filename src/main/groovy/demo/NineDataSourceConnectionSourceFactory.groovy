package demo

import grails.util.Environment
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.jdbc.DataSourceBuilder
import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSource
import org.grails.datastore.gorm.jdbc.connections.DataSourceConnectionSourceFactory
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettingsBuilder
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.connections.ConnectionSource
import org.grails.datastore.mapping.core.connections.ConnectionSourceSettings
import org.springframework.core.env.PropertyResolver

import javax.sql.DataSource

@CompileStatic
class NineDataSourceConnectionSourceFactory extends DataSourceConnectionSourceFactory {

    private final Map<String, ConnectionSource<DataSource, DataSourceSettings>> dataSources = new LinkedHashMap<>();

    protected <F extends ConnectionSourceSettings> DataSourceSettings buildSettings(String name, PropertyResolver configuration, F fallbackSettings, boolean isDefaultDataSource) {
        String configurationPrefix = isDefaultDataSource ? Settings.SETTING_DATASOURCE : Settings.SETTING_DATASOURCES + '.' + name;
        DataSourceSettingsBuilder builder;
        if(isDefaultDataSource) {
            String qualified = Settings.SETTING_DATASOURCES + '.' + Settings.SETTING_DATASOURCE;
            Map config = configuration.getProperty(qualified, Map.class, Collections.emptyMap());
            if(!config.isEmpty()) {
                builder = new DataSourceSettingsBuilder(configuration, qualified);
            }
            else {
                builder = new DataSourceSettingsBuilder(configuration, configurationPrefix);
            }
        }
        else {
            builder = new DataSourceSettingsBuilder(configuration, configurationPrefix);
        }

        DataSourceSettings settings = builder.build();
        return settings;
    }

    @Override
    public ConnectionSource<DataSource, DataSourceSettings> create(String name, PropertyResolver configuration) {
        if(dataSources.containsKey(name)) {
            return dataSources.get(name);
        }
        else {
            ConnectionSource<DataSource, DataSourceSettings> connectionSource = doCreate(name);
            dataSources.put(name, connectionSource);
            return connectionSource;
        }
    }

    @Override
    public ConnectionSource<DataSource, DataSourceSettings> create(String name, DataSourceSettings settings) {
        if(dataSources.containsKey(name)) {
            return dataSources.get(name);
        }
        else {
            ConnectionSource<DataSource, DataSourceSettings> connectionSource = doCreate(name);
            dataSources.put(name, connectionSource);
            return connectionSource;
        }
    }

    //Original bean in hibernate plugin uses dataSource configuration for creating beans, its hard coded here.
    ConnectionSource<DataSource, DataSourceSettings> doCreate(String name) {
        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder(getClass().getClassLoader())

        //TODO - this will need to come from config based on different env.
        DataSourceSettings settings = DataSourceSettingsBuilderHelper.buildDefault()

        dataSourceBuilder.setPooled(settings.pooled)
        dataSourceBuilder.setReadOnly(settings.readOnly)

        if(settings.properties != null && !settings.properties.isEmpty()) {
            dataSourceBuilder.properties(settings.toProperties());
        }

        dataSourceBuilder.url(settings.url)
        dataSourceBuilder.driverClassName(settings.driverClassName);
        dataSourceBuilder.username(settings.username)
        dataSourceBuilder.password(settings.password)

        if (settings.type != null) {
            dataSourceBuilder.type(settings.type)
        }

        DataSource dataSource = dataSourceBuilder.build();
        dataSource = proxy(dataSource, settings);
        return new DataSourceConnectionSource(name, dataSource, settings);
    }

}
