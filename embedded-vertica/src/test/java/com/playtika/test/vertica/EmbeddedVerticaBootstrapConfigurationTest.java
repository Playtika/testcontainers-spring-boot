package com.playtika.test.vertica;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(
        classes = EmbeddedVerticaBootstrapConfigurationTest.TestConfiguration.class,
        properties = {
                "spring.profiles.active=enabled",
                "embedded.vertica.enabled=true"
        }
)
class EmbeddedVerticaBootstrapConfigurationTest {
    @Autowired
    ConfigurableListableBeanFactory beanFactory;

    @Autowired
    JdbcTemplate verticaJdbcTemplate;

    @Autowired
    ConfigurableEnvironment environment;

    @Test
    public void shouldConnectToVertica() {
        assertThat(verticaJdbcTemplate.queryForObject("SELECT version()", String.class)).contains("Vertica Analytic Database");
    }

    @Test
    public void propertiesAreAvailable() {
        assertThat(environment.getProperty("embedded.vertica.port")).isNotEmpty();
        assertThat(environment.getProperty("embedded.vertica.host")).isNotEmpty();
        assertThat(environment.getProperty("embedded.vertica.database")).isNotEmpty();
        assertThat(environment.getProperty("embedded.vertica.user")).isNotEmpty();
        assertThat(environment.getProperty("embedded.vertica.password")).isNotNull();
    }

    @EnableAutoConfiguration
    @Configuration
    static class TestConfiguration {
        @Value("${spring.datasource.url}")
        String jdbcUrl;

        @Value("${spring.datasource.username}")
        String user;

        @Value("${spring.datasource.password}")
        String password;

        @Value("${spring.datasource.driver-class-name}")
        String driverClassName;

        @Bean
        public DataSource verticaDataSource() {
            return DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(jdbcUrl)
                    .username(user)
                    .password(password)
                    .build();
        }

        @Bean
        public JdbcTemplate verticaJdbcTemplate(DataSource verticaDataSource) {
            return new JdbcTemplate(verticaDataSource);
        }
    }
}
