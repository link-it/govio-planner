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
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import it.govhub.govio.planner.batch.jobs.GovioPlannerConfig;
import it.govhub.govio.planner.batch.service.GovioPlannerBatchService;


@SpringBootApplication(scanBasePackages={"it.govhub.govio.planner"})
@EnableScheduling
public class Application extends SpringBootServletInitializer {
	
		
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

	@Scheduled(cron = "${planner.ntfy.schedule.time}", zone = "${planner.ntfy.schedule.zone}")
	public void fileProcessingJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobExecutionException, NoSuchJobException   {
		this.log.info("Running scheduled {}", GovioPlannerConfig.PLANNERJOB);
		try {
			this.govioBatches.runPlannerJob();
		}
		catch(JobInstanceAlreadyCompleteException e) {
			log.debug("Il batch ha già girato con successo oggi");
		}
	}
}