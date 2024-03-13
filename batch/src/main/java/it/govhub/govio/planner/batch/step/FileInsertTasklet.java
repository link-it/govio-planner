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
package it.govhub.govio.planner.batch.step;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
/*
 * 
 * Tasklet che ha il compito di salvare sul database il nuovo file creato.
 * 
 * 
 */
import it.govhub.govio.planner.batch.repository.ExpirationCIEFileRepository;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;

public class FileInsertTasklet implements Tasklet {
	@Value("${planner.ntfy.csv-dir}")
	private Path notifyFile;

	@Autowired
	ExpirationCIEFileRepository expirationCIEFileRepository;

	@Autowired
	GovioFileProducedRepository govioFileProducedRepository;

	private Logger logger = LoggerFactory.getLogger(FileInsertTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws IOException {
		
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		String fileName = jobExecutionContext.getString("destFilename");
		
		// path del file creato nel writ
		Path location = notifyFile.resolve(fileName);
		Long numRows = 0L;
		if (jobExecutionContext.containsKey("NumRows") == true) {
			numRows = jobExecutionContext.getLong("NumRows");
		} else {
			numRows = Files.lines(location).count()-1;
		}
	    ExpirationCIEFileEntity expirationFile = expirationCIEFileRepository.lastExpirationFile();
		GovioFileProducedEntity govioFileProducedEntity = GovioFileProducedEntity.builder().
				creationDate(OffsetDateTime.now()).
				expirationFile(expirationFile).
				location(location).
				messageCount(numRows).
				name(fileName).
				size(location.toFile().length()).
				status(Status.SCHEDULED).build();
		
		govioFileProducedRepository.save(govioFileProducedEntity);
		
		expirationFile.setProcessingDate(OffsetDateTime.now());
		expirationCIEFileRepository.save(expirationFile);
		
		logger.info("inserita new entry nella tabella govio_planner_ntfy_files");
		return RepeatStatus.FINISHED;
	}
}
