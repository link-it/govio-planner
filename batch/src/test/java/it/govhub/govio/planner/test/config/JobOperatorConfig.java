package it.govhub.govio.planner.test.config;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JobOperatorConfig {
	
	@Autowired
	JobRegistry jobRegistry;


	/**
	 * Definire questo bean fa si che il jobOperator venga a conoscenza delle JobInstance esistenti e possa riavviarle.
	 * 
	 */
	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry);
		return postProcessor;
	}
}
