# J2EEでCAサービス利用APPを実装してみる。

## サーバ設定。

試験的にWildfly10で動きました。データソース設定は下記。
INSTALL_DIR/standalone/configuration/standalone.xml


        <subsystem xmlns="urn:jboss:domain:datasources:4.0">
            <datasources>
                <datasource jndi-name="java:jboss/datasources/ExampleDS" pool-name="ExampleDS" enabled="true" use-java-context="true">
                    <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE</connection-url>
                    <driver>h2</driver>
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>
                <datasource jta="true" jndi-name="java:jboss/datasources/CAServiceConsume" pool-name="CAServiceConsume" enabled="true" use-java-context="true" use-ccm="true">
                    <connection-url>jdbc:mysql://10.0.2.2:3306/ca_service_consume</connection-url>
                    <driver>mysql</driver>
                    <security>
                        <user-name>ca</user-name>
                        <password>password</password>
                    </security>
                    <statement>
                        <prepared-statement-cache-size>32</prepared-statement-cache-size>
                        <share-prepared-statements>true</share-prepared-statements>
                    </statement>
                </datasource>
                <drivers>
                    <driver name="h2" module="com.h2database.h2">
                        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
                    </driver>
                    <driver name="mysql" module="com.mysql">
                        <xa-datasource-class>com.mysql.jdbc.jdbc2.optional.MysqlXADataSource</xa-datasource-class>
                    </driver>
                </drivers>
            </datasources>
        </subsystem>

モジュール設定

INSTALL_DIR/modules/system/layers/base/com/mysql/main/mysql-connector-java-5.1.42-bin.jar


INSTALL_DIR/modules/system/layers/base/com/mysql/main/module.xml

        <module xmlns="urn:jboss:module:1.1" 
                name="com.mysql">
            <resources>
                <resource-root path="mysql-connector-java-5.1.42-bin.jar"/>
                <!-- Insert resources here -->
            </resources>
            <dependencies>
                <module name="javax.api"/>
                <module name="javax.transaction.api"/>
                <module name="javax.servlet.api" optional="true"/>
            </dependencies>
        </module>

## 利用方法

1. メニュー画面

　　`http://192.168.56.11:8080/CAServiceConsumer/user_man.jsf`

