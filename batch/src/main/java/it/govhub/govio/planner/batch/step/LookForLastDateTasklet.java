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

import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.bean.MyClock;
import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
/*
 * 
 * Tasklet che ha il compito di individuare nel database la data in cui è girato l'ultima volta il batch e di calcolare la data di spedizione dei messaggi di notifica.
 * Se non c'è una iterazione precedente del batch, viene presa la data dal file di properties.
 * 
 */
public class LookForLastDateTasklet implements Tasklet {
	
	@Autowired
	MyClock myClock;

	@Value("${planner.ntfy.recoveryDays:7}")
	private int recoveryDays;
	
	@Value("${planner.ntfy.expedition-delay-hours:0}")
	private Long expeditionDelayHours;
	
	@Value("${planner.ntfy.schedule.zone:Europe/Rome}")
	private ZoneId zone;

	@Autowired
	GovioFileProducedRepository govioFileProducedRepository;

	private Logger logger = LoggerFactory.getLogger(LookForLastDateTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
		
		OffsetDateTime date = govioFileProducedRepository.lastDateNotifyFile();
		if (date == null) {
			// Non ci sono esecuzioni, e' una nuova installazione.
			// Imposto come ultima esecuzione ieri.
			date = myClock.now().minusDays(1);
			logger.warn("Data di ultima esecuzione non presente. Viene impostata al {}.", date);
		}
		
		OffsetDateTime dateTreshold = myClock.now().minusDays(recoveryDays);
		if (date.isBefore(dateTreshold)) {
			logger.warn("Data di ultima esecuzione {} troppo remota. Viene anticipata al {}.", date, dateTreshold);
			date = dateTreshold;
		}
		
		OffsetDateTime expeditionDate = myClock.now().plusHours(expeditionDelayHours);		
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		
		logger.info("Data in cui è girato il batch l'ultima volta: {}", date);
		logger.info("Expedition date: {}",expeditionDate);
		
		jobExecutionContext.put("date", date.toEpochSecond());
		jobExecutionContext.put("expeditionDate",expeditionDate.toEpochSecond());
		
		return RepeatStatus.FINISHED;
	}
	
}