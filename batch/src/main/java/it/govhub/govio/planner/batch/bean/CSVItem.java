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
package it.govhub.govio.planner.batch.bean;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class CSVItem {
	String fullName;
	String birthdate;
	String address;
	String identityCardNumber;
	String dueDate;
	String CAP;
	String age;
	String gender;
	String releaseDate;
	String taxCode;
	String codPositionAnagrafica;
	
	private final Logger logger = LoggerFactory.getLogger(CSVItem.class);

    public void setFullName(String fullName) {
    	this.fullName = fullName.trim();
    }
    
    public void setIdentityCardNumber(String identityCardNumber) {
    	this.identityCardNumber = identityCardNumber.trim();
    }
    
    public void setDueDate(String dueDate) {
    	this.dueDate = dueDate.trim();
    }
    
    public void setReleaseDate(String releaseDate) {
    	this.releaseDate = releaseDate.trim();
    }
    
    public void setTaxCode(String taxCode) {
    	this.taxCode = taxCode.trim();
    }    
    
    public boolean validate(CSVItem item) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    	try {
            LocalDate.parse(item.getReleaseDate(), formatter);
    	}
        catch (DateTimeParseException e) {
        	logger.warn("data di rilascio della carta di identità: {}, sintatticamente errata",e.getParsedString());
        	return false;
        }
    	try {
            LocalDate.parse(item.getDueDate(), formatter);
    	}
    	catch (DateTimeParseException e) {
    		logger.warn("data di scadenza della carta di identità: {}, sintatticamente errata",e.getParsedString());
    		return false;
    	}

    	if (!Pattern.matches("^[a-zA-Z\\s]*$",item.getFullName())) {
    		logger.warn("valore {} del csv sintatticamente errato per il campo nome completo",item.getFullName());
    		return false;
    	}
    	if (!Pattern.matches("^[A-Z]{2}\\d{7}$",item.getIdentityCardNumber())) {
    		logger.warn("valore {} del csv sintatticamente errato per il campo numero di carta d'identità",item.getIdentityCardNumber());
    		return false;
    	}
    	if (!Pattern.matches("^[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]$",item.getTaxCode())) {
    		logger.warn("valore {} del csv sintatticamente errato per il campo codice fiscale",item.getTaxCode());
    		return false;
    	}
    	return true;
    }
}








