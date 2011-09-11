/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.springframework.amqp.amqp190;

import javax.sql.DataSource;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Basic spring configuration for this test.
 * 
 * @author René X. Parra
 */
@Configuration
public class TestConfiguration {
    @Value("${rabbit.host}") private String host;
    @Value("${rabbit.port}") private int port;
    @Value("${rabbit.username}") private String username;
    @Value("${rabbit.password}") private String password;
    @Value("${rabbit.virtualHost}") private String virtualHost;
    @Value("${rabbit.managementPort}") private int managementPort;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
    
    @Bean
    public DataSource dataSource() {
        EmbeddedDatabase db = new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .setName("h2-test-db")
            .build();
        return db;
    }
    
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(Amqp190Test.TOPIC_EXCHANGE_NAME, true, true);
    }
    
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connectionFactory.setChannelCacheSize(3); // number of actual persistent channels should not be greater than 3
        return connectionFactory;
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory());
    }
    
    @Bean
    public RabbitManagementTemplate rabbitManagementTemplate() {
        RabbitManagementTemplate mgmtTemplate = new RabbitManagementTemplate();
        mgmtTemplate.setHost(host);
        mgmtTemplate.setPort(managementPort);
        mgmtTemplate.setUsername(username);
        mgmtTemplate.setPassword(password);
        return mgmtTemplate;
    }
    
    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }
}
