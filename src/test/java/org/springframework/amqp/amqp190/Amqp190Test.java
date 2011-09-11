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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Test
    public void testDataConnection() throws Throwable {
        int n = jdbcTemplate.queryForInt("SELECT 2");
        assertThat(n, is(2));
    }
}
