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
package it.govhub.govio.planner.batch.step;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

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
		logger.debug("LookForFileTasklet, recupero ultimo file caricato.");
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
		jobExecutionContext.put("destFilename", "CIE_EXPIRATION_"+LocalDate.now()+"-"+UUID.randomUUID()+".csv");

		logger.info("Trovato il path del file delle scadenze {}",expFile.getLocation());
		return RepeatStatus.FINISHED;
	}
}