/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.google.api.Google;
import org.springframework.social.google.connect.GoogleConnectionFactory;


@Configuration
@EnableSocial
public class SocialConfig extends SocialConfigurerAdapter {
	@Override
	public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer, Environment environment) {
	    GoogleConnectionFactory googleConnectionFactory = new GoogleConnectionFactory(environment.getProperty("spring.social.google.appId"), environment.getProperty("spring.social.google.appSecret"));
	    googleConnectionFactory.setScope("https://www.googleapis.com/auth/plus.login");
	    connectionFactoryConfigurer.addConnectionFactory(googleConnectionFactory);
	}
	
	@Bean
	@Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
	public Google google(ConnectionRepository repository) {
	    Connection<Google> connection = repository.findPrimaryConnection(Google.class);
	    return connection != null ? connection.getApi() : null;
	}
}