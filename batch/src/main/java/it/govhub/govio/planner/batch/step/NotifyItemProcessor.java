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

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import it.govhub.govio.planner.batch.bean.CSVExpiration;
import it.govhub.govio.planner.batch.bean.CSVItem;
import it.govhub.govio.planner.batch.bean.MyClock;

/*
 * Classe che processa gli elementi del file csv, selezionando le righe la cui data di scadenza rientra nella finestra di preavviso specificata nel file di properties.
 * Viene controllata dal preavviso più recente al più remoto. Se una entry risulta sintatticamente non valida, viene ignorata e viene loggato un messaggio di warning.
 * I formati delle date sono configurabili nelle application properties 
 */
public class NotifyItemProcessor implements ItemProcessor<CSVItem, CSVExpiration> {
	
	@Value("${planner.ntfy.policy}")
	private String policy;
	
	@Value("${planner.ntfy.schedule.zone:Europe/Rome}")
	private String zone;


	private Logger logger = LoggerFactory.getLogger(NotifyItemProcessor.class);

	private OffsetDateTime lastExecuted;
	private OffsetDateTime expeditionDateTime;
	private long dateLastExecutedTimestamp;
	private long expeditionDateTimestamp;
	
	public NotifyItemProcessor(long dateLastExecutedTimestamp,long expeditionDateTimestamp){
		this.expeditionDateTimestamp = expeditionDateTimestamp;
		this.dateLastExecutedTimestamp = dateLastExecutedTimestamp;
	}

	@Autowired
	MyClock myClock;
	
	@Override
	public CSVExpiration process(CSVItem item) {
		// controlla che i valori della riga del csv siano tutti sintatticamente validi in caso contrario salta la riga
		if (item.validate(item) == false) return null;
		
		this.expeditionDateTime = OffsetDateTime.ofInstant(Instant.ofEpochSecond(expeditionDateTimestamp), ZoneOffset.of(zone));
		this.lastExecuted = OffsetDateTime.ofInstant(Instant.ofEpochSecond(dateLastExecutedTimestamp), ZoneOffset.of(zone));

		logger.info("Riga: {} validata con successo",item);

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dueDate = LocalDate.parse(item.getDueDate(),formatter);

		String[] splits = policy.split(",");
		for (int i = splits.length-1; i>=0; i--) {
			CSVExpiration res = compareDates(lastExecuted.toLocalDate(), expeditionDateTime.toLocalDate(), dueDate, Integer.valueOf(splits[i]),item);
			if (res != null) {
				logger.info("Riga: {} aggiunta alle righe da inserire nel CSV",item);
				return res;
			}
		  }
		logger.info("Riga: {} saltata perchè la scadenza non rientra nelle finestre di preavviso",item);
		return null;
	}
	
	private CSVExpiration compareDates(LocalDate lastExecuted,LocalDate expeditionDate, LocalDate dueDate, int days, CSVItem item) {
		if (
				(lastExecuted.plusDays(days).compareTo(dueDate) < 0)
				&&
				(myClock.now().toLocalDate().plusDays(days).compareTo(dueDate) >= 0)
				)
			return new CSVExpiration(item.getTaxCode(),expeditionDateTime.toString(),item.getDueDate(),item.getFullName(),item.getIdentityCardNumber(),item.getReleaseDate(),Integer.toString(days));
		return null;
	}
}

