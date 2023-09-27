package demo

import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings
import org.grails.datastore.mapping.core.connections.ConnectionSourceSettings
import org.grails.orm.hibernate.cfg.Settings
import org.grails.orm.hibernate.connections.HibernateConnectionSourceFactory
import org.grails.orm.hibernate.connections.HibernateConnectionSourceSettings
import org.springframework.core.env.PropertyResolver

class NineHibernateConnectionSourceFactory extends HibernateConnectionSourceFactory {

    NineHibernateConnectionSourceFactory(Class... classes) {
        super(classes)
    }

    @Override
    protected <F extends ConnectionSourceSettings> HibernateConnectionSourceSettings buildSettings(String name, PropertyResolver configuration, F fallbackSettings, boolean isDefaultDataSource) {
        HibernateConnectionSourceSettings settings = new HibernateConnectionSourceSettings()
        if(isDefaultDataSource) {
            DataSourceSettings dataSourceSettings = DataSourceSettingsBuilderHelper.buildDefault()
                settings.setDataSource(dataSourceSettings);
        }
        else {
            String prefix = Settings.SETTING_DATASOURCES + "." + name;
            settings = buildSettingsWithPrefix(configuration, fallbackSettings, prefix);
        }
        return settings;
    }
}
