package com.yowyob.dev.configuration;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private final Environment env;

    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public DataSource getDataSource() {
        System.out.println("### CONFIGURING JDBC DATASOURCE FOR FLYWAY ###");
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver"); // Driver JDBC classique
        dataSourceBuilder.url(env.getProperty("spring.datasource.url"));
        dataSourceBuilder.username(env.getProperty("spring.datasource.username"));
        dataSourceBuilder.password(env.getProperty("spring.datasource.password"));
        return dataSourceBuilder.build();
    }

    // Optionnel, mais peut aider à voir si Flyway est bien appelé
    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        System.out.println("### CONFIGURING FLYWAY MIGRATION STRATEGY ###");
        return flyway -> {
            // Vous pouvez ajouter une logique ici si nécessaire, par ex:
            // flyway.clean(); // Attention: efface tout ! A n'utiliser qu'en dev.
            flyway.migrate();
        };
    }
}