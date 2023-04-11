package it.govhub.govio.planner.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import it.govhub.security.config.GovhubSecurityConfig;


/**
 * Configurazione della sicurezza
 * 
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends GovhubSecurityConfig {
	

}
