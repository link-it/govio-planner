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
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import it.govhub.govio.planner.batch.jobs.GovioExpeditionJob;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;
import it.govhub.govio.planner.batch.service.GovioPlannerBatchService;
import it.govhub.govio.planner.batch.exception.ExpeditionDateFileNotExists;

@SpringBootApplication(scanBasePackages={"it.govhub.govio.planner", "it.govhub.govio.v1"})
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
	
	@Scheduled(cron = "${planner.ntfy.schedule.time}", zone = "${planner.ntfy.schedule.zone:Europe/Rome}")
	public void fileProcessingJob() throws Exception  {
		this.log.debug("Running scheduled {}", GovioPlannerJob.PLANNERJOB);
		try {
			this.govioBatches.runPlannerJob();
		}
		catch(JobInstanceAlreadyCompleteException e) {
			log.debug("Il batch ha già girato con successo oggi");
		}
		catch(ExpeditionDateFileNotExists e2) {
			log.debug(e2.getMessage());
		}
	}
	
	
	@Scheduled(fixedDelayString = "${planner.fileExpeditionJob.fixedDelay:30000}", initialDelayString = "${planner.fileExpeditionJob.initialDelay:1}")
	public void expeditionJob() throws Exception  {
		this.log.debug("Running scheduled {}", GovioExpeditionJob.GOVIO_PLANNER_EXPEDITION_JOB);
		this.govioBatches.runExpeditionJob();
	}
}
