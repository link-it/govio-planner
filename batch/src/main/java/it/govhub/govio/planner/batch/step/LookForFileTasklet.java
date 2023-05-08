package it.govhub.govio.planner.batch.step;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;

/*
 * 
 * Tasklet che ha il compito di individuare il file delle scadenze.
 * 
 * 
 */
public class LookForFileTasklet implements Tasklet {
	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;

	private Logger logger = LoggerFactory.getLogger(LookForFileTasklet.class);


	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws IOException {
		ExpirationCIEFileEntity expFile = expirationCIEFileRepository.lastExpirationFile();
		if (expFile==null) {
			logger.error("Il file delle scadenze è assente nel database");
			throw new IOException("Il tracciato delle notifiche è assente nel database");
		}
		File exp = new File(expFile.getLocation());
		if (exp.getAbsolutePath().isEmpty() || !exp.canRead()) {
			logger.error("Il file delle scadenze è assente o non è leggibile");
			throw new IOException("Il file delle scadenze è assente o non è leggibile");
		}
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		jobExecutionContext.put("location", expFile.getLocation());
		logger.info("Trovato il path del file delle scadenze {}",expFile.getLocation());
		return null;
	}
}