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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;

import it.govhub.govio.planner.batch.repository.GovioFileProducedRepository;
/*
 * 
 * Tasklet che ha il compito di individuare nel database la data in cui è girato l'ultima volta il batch e di calcolare la data di spedizione dei messaggi di notifica.
 * Se non c'è una iterazione precedente del batch, viene presa la data dal file di properties.
 * 
 */
public class LookForLastDateTasklet implements Tasklet {

	@Value("${planner.ntfy.min-last-date}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate minLastDate;
	
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
			// L'epoch viene intesa come UTC		
			date = OffsetDateTime.of(minLastDate, LocalTime.MIDNIGHT, ZoneOffset.UTC);
		}
		OffsetDateTime expeditionDate = OffsetDateTime.now(zone).plusHours(expeditionDelayHours);		
		ExecutionContext jobExecutionContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
		
		logger.info("Data in cui è girato il batch l'ultima volta: {}",date);
		logger.info("Expedition date: {}",expeditionDate);
		
		jobExecutionContext.put("date", date.toEpochSecond());
		jobExecutionContext.put("expeditionDate",expeditionDate.toEpochSecond());
		
		return RepeatStatus.FINISHED;
	}
}