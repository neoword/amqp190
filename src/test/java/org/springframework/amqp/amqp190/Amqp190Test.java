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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Tests the theory that spring-amqp-1.0.0.RELEASE "leaks" channels when 
 * channelTransacted = true, and when within the scope of a transaction.
 * 
 * @author René X. Parra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:/testContext.xml")
public class Amqp190Test {
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Amqp190Test.class);
    
    public static final String TOPIC_EXCHANGE_NAME = "amqp190.topic";
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RabbitManagementTemplate rabbitManagementTemplate;
    @Value("${local.ip}") 
    private String localIp;
    
    // Check DB connectivity
    @Test
    public void dataConnection() throws Throwable {
        int n = jdbcTemplate.queryForInt("SELECT 2");
        assertThat(n, is(2));
    }

    @Test
    public void publishWithTransactedFalseWithoutTransaction() {
        // This test passes
        testRabbit(false, false);
    }
    
    @Test
    public void publishWithTransactedFalseWithTransaction() {
        // This test passes
        testRabbit(false, true);
    }
    
    @Test
    public void publishWithTransactedTrueWithoutTransaction() {
        // This test passes
        testRabbit(true, false);
    }
    
    @Test
    public void publishWithTransactedTrueWithTransaction() {
        // This test fails
        testRabbit(true, true);
    }
    
    private void testRabbit(final boolean channelTransacted, boolean withTransaction) {
        rabbitTemplate.setChannelTransacted(channelTransacted);
        int n=50;
        for(int i=0;i<n;++i) {
            if(!withTransaction) {
                rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, "sampleRK", "sample message");
            } else {
                TransactionTemplate tt = new TransactionTemplate(transactionManager);
                tt.execute(new TransactionCallback<Object>() {
                    public Object doInTransaction(TransactionStatus status) {
                        jdbcTemplate.queryForInt("SELECT 2");
                        rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_NAME, "sampleRK", "sample message");
                        return null;
                    }
                });
            }
        }
        ConnectionInfo connectionInfo;
        connectionInfo = rabbitManagementTemplate.getConnectionInfo(localIp);
        CachingConnectionFactory connectionFactory = (CachingConnectionFactory) rabbitTemplate.getConnectionFactory();
        assertThat(String.format("Expected less than %s, actual is %s", connectionFactory.getChannelCacheSize(), connectionInfo.getNumChannels()),
                connectionInfo.getNumChannels() <= connectionFactory.getChannelCacheSize(), is(true));
    }

}
