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
package it.govhub.govio.planner.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.MapperFeature;

import it.govhub.govio.planner.batch.jobs.GovioExpeditionJob;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;
import it.govhub.govio.planner.batch.service.GovioPlannerBatchService;


@SpringBootApplication(scanBasePackages={"it.govhub.govio.planner", "it.govhub.govio.v1"})
@EnableScheduling
public class Application  {
	
	private Logger log = LoggerFactory.getLogger(Application.class);
	
	public static final String GOVIOJOBID_STRING = "GovioJobID";

	@Autowired
	@Qualifier("PlannerJob") 
	private Job plannerJob;
	
	@Autowired
	GovioPlannerBatchService govioBatches;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		log.info("Building the Jackson Object mapper customizer...");
		
		return builder ->  builder.
				featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

	}
	

	//@Scheduled(cron = "${planner.ntfy.schedule.time}", zone = "${planner.ntfy.schedule.zone:Europe/Rome}")
	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000000}", initialDelayString = "${scheduler.initialDelayString:1}")
	public void fileProcessingJob() throws Exception  {
		this.log.info("Running scheduled {}", GovioPlannerJob.PLANNERJOB);
		try {
			this.govioBatches.runPlannerJob();
		}
		catch(JobInstanceAlreadyCompleteException e) {
			log.info("Il batch ha già girato con successo oggi");
		}
	}
	
	
	@Scheduled(fixedDelayString = "${scheduler.fileProcessingJob.fixedDelayString:10000}", initialDelayString = "${scheduler.initialDelayString:5000}")
	public void expeditionJob() throws Exception  {
		this.log.info("Running scheduled {}", GovioExpeditionJob.GOVIO_PLANNER_EXPEDITION_JOB);
		this.govioBatches.runExpeditionJob();
	}
}