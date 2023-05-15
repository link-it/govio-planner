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
package it.govhub.govio.planner.batch.service;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import it.govhub.govio.planner.batch.bean.MyClock;
import it.govhub.govio.planner.batch.jobs.GovioPlannerJob;

@Service
public class GovioPlannerBatchService {

	private static final String PLANNERJOB = GovioPlannerJob.PLANNERJOB;
	public static final String PLANNERJOB_ID = "GovioPlannerJobID";

	private LocalDate CURRENTDATE_STRING;


	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	JobRepository jobRepository;
	
	@Autowired
	JobExplorer jobExplorer;
	
	@Autowired
	JobOperator jobOperator;

	@Autowired
	@Qualifier("PlannerJob")
	private Job plannerJob;
	
	@Autowired
	MyClock myClock;
	
	private Logger log = LoggerFactory.getLogger(GovioPlannerBatchService.class);
	

public JobExecution runPlannerJob() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException, NoSuchJobExecutionException, NoSuchJobException  {
	
	CURRENTDATE_STRING = myClock.now();
	
	JobInstance lastInstance = this.jobExplorer.getLastJobInstance(PLANNERJOB);
	
	// Determino i JobParameters con cui lanciare il Job. In base al loro valore avverrà un avvio nuovo, un restart, o nulla.
	JobParameters params = null;
	JobExecution lastExecution = null;
	
	if (lastInstance != null) {
		lastExecution = this.jobExplorer.getLastJobExecution(lastInstance);
	}
	
	if (lastInstance != null && lastExecution == null) {
		log.error("Trovata istanza preesistente per il job [{}] ma senza una JobExecution associata, forse l'esecuzione deve ancora partire. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", PLANNERJOB);
		return null;
	}
	else if (lastExecution != null) {
		ExitStatus exitStatus = lastExecution.getExitStatus();
		
		// L'Exit Status di un Job è così determinato:
		// 			- 	If the Step ends with ExitStatus of FAILED, the BatchStatus and ExitStatus of the Job are both FAILED.
		// 			-	Otherwise, the BatchStatus and ExitStatus of the Job are both COMPLETED.
		//		https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#batchStatusVsExitStatus
		//
		// In questo caso batchStatus e exitStatus combaciano perchè non c'è nessuna logica particolare nel GovioPlannerConfig
		// che altera lo stato del job nel caso gli step falliscano.
		switch (lastExecution.getStatus()) {

		// In questo caso Creo un nuovo Job.
		case ABANDONED:
			log.warn("Trovata Job Execution di id {} abbandonata!", lastExecution.getId());
		case COMPLETED:
		
			// I Job Abandoned non possono essere riavviati. (Sono abbandonati appunto)
			// https://docs.spring.io/spring-batch/docs/current/reference/html/index-single.html#aborting-a-job
			// Se è in stato abandoned allora assumiamo che sia stata una scelta del programmatore o di un operatore del batch metterlo in quello stato.
			// Siamo liberi di andare avanti e di eseguire un nuovo job.
			log.info("Trovata istanza preesistente per il Job [{}]. Avvio nuovo Job. ", lastExecution); //GOVIO_PLANNER_JOB_ID, exitStatus, lastExecution.getStatus());
			params = new JobParametersBuilder()
					.addString("When", CURRENTDATE_STRING.toString())
					.addString(PLANNERJOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
			return jobLauncher.run(plannerJob, params);
		
		// In questo caso riavvio.
		case FAILED:
		case STOPPED:
			log.info("Trovata istanza preesistente per il Job [{}]. Riavvio il Job. ", lastExecution); //FileProcessingJobConfig.FILEPROCESSING_JOB, exitStatus, lastExecution.getStatus());
			Long newExecutionId = jobOperator.restart(lastExecution.getId());
			return jobExplorer.getJobExecution(newExecutionId);
		default:
			// STARTED, STARTING, STOPPING, UNKNOWN:
			// STARTED STARTING e STOPPING non dovremmo mai trovarli, per via del comportamento dello scheduler.
			
			// UNKNOWN - Questo possiamo scoprirlo solo operativamente.
			log.info("Trovata istanza preesistente per il Job [{}]. STATO INASPETTATO. Nessun Job avviato, se la situazione persiste anche nelle prossime run è richiesto un'intervento manuale.", lastExecution); //GOVIO_PLANNER_JOB_ID, exitStatus, lastExecution.getStatus());
			return null;
		}
	}	else {
		params = new JobParametersBuilder()
				.addString("When", CURRENTDATE_STRING.toString())
				.addString(PLANNERJOB_ID, GovioPlannerJob.PLANNERJOB).toJobParameters();
		return jobLauncher.run(plannerJob, params);
}
}
}
