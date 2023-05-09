/*******************************************************************************
 *  GovIO Planner - Notification system Planner for AppIO
 *  
 *  Copyright (c) 2021-2023 Link.it srl (http://www.link.it).
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3, as published by
 *  the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *******************************************************************************/
package it.govhub.govio.planner.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govregistry.commons.config.CommonsExportedBeans;
import it.govhub.govregistry.commons.config.TimeZoneConfigurer;
import it.govhub.govregistry.commons.exception.handlers.RequestRejectedExceptionHandler;
import it.govhub.govregistry.commons.utils.Base64String;
import it.govhub.govregistry.commons.utils.Base64StringSerializer;
import it.govhub.govregistry.readops.api.config.ReadOpsExportedBeans;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@EnableJpaRepositories("it.govhub.govio.planner.api.repository")
@ComponentScan( {"it.govhub.govio.planner" })
@EntityScan("it.govhub.govio.planner.api.entity")
@Import({ ReadOpsExportedBeans.class,  CommonsExportedBeans.class,   TimeZoneConfigurer.class})
public class Application  extends SpringBootServletInitializer {
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Value("${govhub.time-zone:Europe/Rome}")
	String timeZone;


	/**
	 * Gestisce la famiglia di header X-Forwarded o lo header Forwarded.
	 * Utile per la scrittura dei link hateoas in modo che tengano conto della presenza del proxy. 
	 */
	@Bean
	public ForwardedHeaderFilter forwardedHeaderFilter() {
		return new ForwardedHeaderFilter();
	}

	/**
	 * Modifichiamo il serializzatore JSON in modo da serializzare le Base64String
	 * come stringhe normali
	 */
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return builder ->  builder.
				timeZone(this.timeZone).
				serializerByType(Base64String.class, new Base64StringSerializer()).
				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	}

	@Configuration
	static class WebMvcConfig implements WebMvcConfigurer {
		
		Logger log = LoggerFactory.getLogger(WebMvcConfig.class);
		
		/**
		 * Questa  serve per serializzare correttamente gli enum passati via
		 * parametro query. Altrimenti è necessario passarli in upperCase.
		 *
		 */
		@Override
		public void addFormatters(FormatterRegistry registry) {
			ApplicationConversionService.configure(registry);
		}
		
		/**
		 * Ignoriamo lo header Accept, avendo un solo content-type da restituire per endpoint.
		 * Disabilitiamo di fatto la content-negotiation.
		 */
		@Override
		public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
			log.info("Configuring the content negotiator...");
		    configurer.
		    favorParameter(false).
		    ignoreAcceptHeader(true).
		    defaultContentType(MediaType.parseMediaType("application/hal+json"), MediaType.ALL);
		}
	}
	
	
	/**
	 * Questo Bean Restituisce un Problem quando spring-security rifiuta una
	 * richiesta perchè ritenuta ad esempio non sicura.
	 */
	@Bean
	public RequestRejectedHandler requestRejectedHandler() {
	   return new RequestRejectedExceptionHandler();
	}
	
	
}