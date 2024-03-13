/*
 * GovHub - Application suite for Public Administration
 *
 * Copyright (c) 2023-2024 Link.it srl (https://www.link.it).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3, as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package it.govhub.govio.planner.batch.jobs;

import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.exception.ShouldRetryException;
import it.govhub.govio.planner.batch.exception.ShouldSkipException;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
import it.govhub.govio.planner.batch.step.SendGovioFileProcessor;

@Configuration
@EnableBatchProcessing
public class GovioExpeditionJob {
	
	public static final String GOVIO_PLANNER_EXPEDITION_JOB = "GovioPlannerExpeditionJob";
	public static final String GOVIO_PLANNER_EXPEDITION_STEP = "GovioPlannerExpeditionStep";
	
	@Autowired
	protected JobBuilderFactory jobs;
	
	@Autowired
	protected StepBuilderFactory steps;
	
	@Autowired
	protected EntityManager entityManager;
	
	@Autowired
	protected GovioFileProducedRepository govioFileRepo;
	
	public static final String MSG_SENDER_TASK_EXECUTOR_NAME = "govio-planner-msgsender"; 
	
	protected TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor(MSG_SENDER_TASK_EXECUTOR_NAME);
	}
	

	@Bean(name = GOVIO_PLANNER_EXPEDITION_JOB)
	public Job plannerJob(	
			@Qualifier(GOVIO_PLANNER_EXPEDITION_STEP) Step expeditionStep)  {
	    
		return jobs.get(GOVIO_PLANNER_EXPEDITION_JOB).
				start(expeditionStep).
				build();
	}
	
	@Bean
	@Qualifier(GOVIO_PLANNER_EXPEDITION_STEP)
	public Step expeditionStep(	SendGovioFileProcessor sendItemProcessor) {
		
		AsyncItemWriter<GovioFileProducedEntity> writer = new AsyncItemWriter<>();
	    writer.setDelegate(govioDbFileWriter());
	    
	    AsyncItemProcessor<GovioFileProducedEntity, GovioFileProducedEntity> asyncSendItem = new AsyncItemProcessor<>();
	    asyncSendItem.setTaskExecutor(taskExecutor());
	    asyncSendItem.setDelegate(sendItemProcessor);
		
	    return steps.get(GOVIO_PLANNER_EXPEDITION_STEP)
	    		.<GovioFileProducedEntity, Future<GovioFileProducedEntity>>chunk(10)
	    		.reader(scheduledGovioFiles())
	    		.processor(asyncSendItem)
	    		.writer(writer)
	    		.faultTolerant()
	    		.skip(ShouldSkipException.class)
	    		.skipLimit(10)
				.retry(ShouldRetryException.class)
				.retryLimit(5)
				.build();
	    
	}
	
	protected ItemReader<GovioFileProducedEntity> scheduledGovioFiles() {
		final String query = "SELECT msg FROM GovioFileProducedEntity msg JOIN FETCH msg.expirationFile exp_file WHERE msg.status = 'SCHEDULED'";
		
        JpaCursorItemReader<GovioFileProducedEntity> itemReader = new JpaCursorItemReader<>();
        itemReader.setQueryString(query);
        itemReader.setEntityManagerFactory(entityManager.getEntityManagerFactory());
        itemReader.setSaveState(true);
        return itemReader;
    }
	
	
	protected ItemWriter<GovioFileProducedEntity> govioDbFileWriter() {
		var ret = new ItemWriterAdapter<GovioFileProducedEntity>();
		ret.setTargetMethod("save");
		ret.setTargetObject(govioFileRepo);
		return ret;
	}
	
}
