package it.govhub.govio.planner.batch.step;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
/*
 * 
 * Tasklet che ha il compito di salvare sul database il nuovo file creato.
 * 
 * 
 */

public class FileInsertTasklet implements Tasklet {
	@Value("${planner.ntfy.csv-dir}")
	private String notifyFile;

	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;

	@Autowired
	GovioFileProducedRepository govioFileProducedRepository;

	private Logger logger = LoggerFactory.getLogger(FileInsertTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		// path del file creato nel writer
	    String filename = notifyFile+"CIE_EXPIRATION_"+LocalDate.now()+".csv";
	    Path location = Path.of(filename);
	    
	    ExpirationCIEFileEntity expirationFile = expirationCIEFileRepository.lastExpirationFile();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder().creationDate(OffsetDateTime.now())
				.expirationFile(expirationFile).location(location).status(Status.SCHEDULED).build();
		govioFileProducedRepository.save(govioFileProducedEntity);
		
		expirationFile.setProcessingDate(OffsetDateTime.now());
		expirationCIEFileRepository.save(expirationFile);
		
		logger.info("inserita new entry nella tabella govio_planner_ntfy_files");
		return RepeatStatus.FINISHED;
	}
}
