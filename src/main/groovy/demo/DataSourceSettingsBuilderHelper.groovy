package demo

import grails.util.Environment
import org.grails.datastore.gorm.jdbc.connections.DataSourceSettings

class DataSourceSettingsBuilderHelper {

    static DataSourceSettings buildDefault() {
        DataSourceSettings settings = new DataSourceSettings(
                pooled: true,
                readOnly: false,
                driverClassName: "org.h2.Driver",
                username: "sa",
                password: "",
                url: "jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE",
                properties: [:]
        )

        if(Environment.current == Environment.TEST) {
            settings.dbCreate = "create-drop"
        }

        return settings
    }
}
