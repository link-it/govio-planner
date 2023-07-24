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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

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
	private List<Integer> policy;
	
	@Value("${planner.ntfy.schedule.zone:Europe/Rome}")
	private ZoneId zone;

	private Logger logger = LoggerFactory.getLogger(NotifyItemProcessor.class);

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
		logger.debug("Analisi scadenza CIE: {}", item.toString());
		
		if (validate(item) == false) {
			logger.debug("Entry scartata per sintassi non valida");
			return null;
		}
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		DateTimeFormatter formatterDateReleaseDate= DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
		LocalDate dueDate = LocalDate.parse(item.getDueDate(),formatter);
		long dueDateTimestamp= dueDate.toEpochSecond(LocalTime.MIDNIGHT, ZoneOffset.UTC);
		ZonedDateTime duedateDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(dueDateTimestamp), zone);
		LocalDate releaseDate = LocalDate.parse(item.getReleaseDate(),formatter);
		Collections.sort(policy);
		for (int days : policy) {
			if (compareDates(dateLastExecutedTimestamp, dueDateTimestamp, days)) {
				ZonedDateTime expeditionDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(expeditionDateTimestamp), zone);
				CSVExpiration csvExpiration = new CSVExpiration(item.getTaxCode(),expeditionDateTime.toLocalDateTime().toString(),duedateDateTime.toLocalDateTime().toString(),releaseDate.format(formatterDateReleaseDate),item.getFullName(),item.getIdentityCardNumber(),Integer.toString(days));
				logger.info("Scadenza del {} a {} giorni. Aggiunta notifica: {}", dueDate, days, csvExpiration);
				return csvExpiration;
			}
		}
		logger.debug("Nessuna notifica da inviare");
		return null;
	}
	
	/** 
	 * Controllo se la dueDate ricade nella finestra di notifica, ovvero
	 * 
	 * @return
	 */
	private boolean compareDates(long lastExecuted, long dueDate, int days) {
		long nowTimestamp = myClock.now().toEpochSecond();
		logger.trace("lastExecuted: {}, nowTimestamp: {}, dueDate: {}", ZonedDateTime.ofInstant(Instant.ofEpochSecond(lastExecuted),zone), ZonedDateTime.ofInstant(Instant.ofEpochSecond(nowTimestamp),zone),ZonedDateTime.ofInstant(Instant.ofEpochSecond(dueDate),zone));
		return	lastExecuted + daysToSeconds(days) < dueDate && nowTimestamp + daysToSeconds(days) >= dueDate;
	}
	
	private long daysToSeconds(int days) {
		return days*24*60*60;
	}
	
	private boolean validate(CSVItem item) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	try {
            LocalDate.parse(item.getReleaseDate(), formatter);
    	} catch (DateTimeParseException e) {
        	logger.warn("Errore di sintassi nella data di rilascio della carta di identità: {}", item.getReleaseDate());
        	return false;
        }
    	
    	try {
            LocalDate.parse(item.getDueDate(), formatter);
    	} catch (DateTimeParseException e) {
    		logger.warn("Errore di sintassi nella data di scadenza della carta di identità: {}", item.getDueDate());
    		return false;
    	}
    	
    	if (!Pattern.matches("^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$",item.getTaxCode())) {
    		logger.warn("Errore di sintassi nel codice fiscale del titolare della carta di identità: {}", item.getTaxCode());
    		return false;
    	}
    	return true;
    }
}

