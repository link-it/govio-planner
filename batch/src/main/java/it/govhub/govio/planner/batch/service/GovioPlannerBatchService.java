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
package it.govhub.govio.planner.batch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import it.govhub.govio.planner.batch.bean.MyClock;
import it.govhub.govio.planner.batch.jobs.GovioExpeditionJob;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;

@Service
public class GovioPlannerBatchService {

	private static final String GOVIO_JOB_ID = "GOVIO_JOB_ID";

	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobExplorer jobExplorer;
	
	@Autowired
	JobOperator jobOperator;

	@Autowired
	@Qualifier(GovioPlannerJob.PLANNERJOB)
	private Job plannerJob;
	
	@Autowired
	@Qualifier(GovioExpeditionJob.GOVIO_PLANNER_EXPEDITION_JOB)
	private Job expeditionJob;
	
	@Autowired
	MyClock myClock;
	
	private Logger log = LoggerFactory.getLogger(GovioPlannerBatchService.class);
	
	
	public JobExecution runExpeditionJob() throws Exception {
		var params = new JobParametersBuilder()
				.addString("When",  String.valueOf(System.currentTimeMillis()))
				.addString(GOVIO_JOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
		return jobLauncher.run(expeditionJob, params);
	}
	

	public JobExecution runPlannerJob() throws Exception {
	
		String CURRENTDATE_STRING = myClock.now().toLocalDate().toString();
		
		JobInstance lastInstance = this.jobExplorer.getLastJobInstance(GovioPlannerJob.PLANNERJOB);
		
		// Determino i JobParameters con cui lanciare il Job. In base al loro valore avverrà un avvio nuovo, un restart, o nulla.
		JobParameters params = null;
		JobExecution lastExecution = null;
		
		if (lastInstance != null) {
			lastExecution = this.jobExplorer.getLastJobExecution(lastInstance);
		}
		
		if (lastInstance != null && lastExecution == null) {
			log.error("Trovata istanza preesistente per il job [{}] ma senza una JobExecution associata, forse l'esecuzione deve ancora partire. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", GovioPlannerJob.PLANNERJOB);
			return null;
		}
		else if (lastExecution != null) {
			switch (lastExecution.getStatus()) {
	
			// In questo caso Creo un nuovo Job.
			case ABANDONED:
				log.warn("Trovata Job Execution di id {} abbandonata!", lastExecution.getId());
			case COMPLETED:
			
				// I Job Abandoned non possono essere riavviati. (Sono abbandonati appunto)
				// https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#aborting-a-job
				// Se è in stato abandoned allora assumiamo che sia stata una scelta del programmatore o di un operatore del batch metterlo in quello stato.
				// Siamo liberi di andare avanti e di eseguire un nuovo job.
				log.debug("Trovata istanza preesistente per il Job [{}]. Avvio nuovo Job. ", lastExecution); //GOVIO_PLANNER_JOB_ID, exitStatus, lastExecution.getStatus());
				params = new JobParametersBuilder()
						.addString("When", CURRENTDATE_STRING)
						.addString(GOVIO_JOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
				return jobLauncher.run(plannerJob, params);
	
				// In questo caso riavvio.
			case FAILED:
			case STOPPED:
				log.debug("Trovata istanza preesistente per il Job [{}]. Riavvio il Job. ", lastExecution); //FileProcessingJobConfig.FILEPROCESSING_JOB, exitStatus, lastExecution.getStatus());
			//	Long newExecutionId = jobOperator.restart(lastExecution.getId());
			//	return jobExplorer.getJobExecution(newExecutionId);
				params = new JobParametersBuilder()
						.addString("When", CURRENTDATE_STRING)
						.addString(GOVIO_JOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
				return jobLauncher.run(plannerJob, params);

			default:
				// UNKNOWN - Questo possiamo scoprirlo solo operativamente.
				log.warn("Trovata istanza preesistente per il Job [{}] in stato sconosciuto. Avvio nuovo Job. ", lastExecution); //GOVIO_PLANNER_JOB_ID, exitStatus, lastExecution.getStatus());
				params = new JobParametersBuilder()
						.addString("When", CURRENTDATE_STRING)
						.addString(GOVIO_JOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
				return jobLauncher.run(plannerJob, params);
			}
		}	else {
			params = new JobParametersBuilder()
					.addString("When", CURRENTDATE_STRING)
					.addString(GOVIO_JOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
			return jobLauncher.run(plannerJob, params);
		}
	}
}
