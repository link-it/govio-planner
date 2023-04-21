package it.govhub.govio.planner.batch.step;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;

public class NotifyRecordWriterTasklet implements Tasklet {
	@Autowired
	GovioFileProducedRepository govioFileProducedRepository;
	
	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;
	
	private Logger logger = LoggerFactory.getLogger(NotifyRecordWriterTasklet.class);

	Long idExp;
	public NotifyRecordWriterTasklet(Long idExpFile) {
		idExp = idExpFile;
	}

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
			String file = "CIE_EXPIRATION_"+LocalDate.now()+".csv";
			FileWriter myWriter;
			try {
				myWriter = new FileWriter(file);
				myWriter.flush();
				myWriter.close();
				ExpirationCIEFileEntity expirationCIEFileEntity= expirationCIEFileRepository.getReferenceById(idExp);
				GovioFileProducedEntity e = GovioFileProducedEntity.builder().status(Status.CREATED).location(file).creationDate(OffsetDateTime.now()).expirationFile(expirationCIEFileEntity).build();
				govioFileProducedRepository.save(e);
			} catch (IOException e1) {
				logger.error("Ricevuta IOException durante la chiusura del file {}",file);
			}
		return null;
	}
}
