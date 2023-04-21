package it.govhub.govio.planner.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import it.govhub.govio.planner.batch.config.GovioPlannerConfig;
import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;

public class LookForFileTasklet implements Tasklet {
	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;

	private Logger logger = LoggerFactory.getLogger(GovioPlannerConfig.class);


	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		// TODO Auto-generated method stub
		ExpirationCIEFileEntity expFile = expirationCIEFileRepository.lastExpirationFile();
		if (expFile == null) {
			logger.error("Il file delle notifiche è assente");
		}
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		jobExecutionContext.put("idExp", ((expFile==null)? null: expFile.getId()));
		jobExecutionContext.put("location", ((expFile==null)? null: expFile.getLocation()));
		return null;
	}
}
