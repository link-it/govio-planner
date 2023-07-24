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
    
}








