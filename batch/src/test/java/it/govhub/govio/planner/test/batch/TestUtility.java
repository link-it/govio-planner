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
package it.govhub.govio.planner.test.batch;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.govhub.govio.planner.batch.bean.MyClock;

public class TestUtility {
	MyClock myClock;

	TestUtility(MyClock myClock) {
		this.myClock = myClock;
	}
	
	public String convertToCSVInput(String[] data) {
	    return Stream.of(data)
	//      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(";"));
	}
	
	
	public String convertToCSVOutput(String[] data) {
	    return Stream.of(data)
	//      .map(this::escapeSpecialCharacters)
	      .collect(Collectors.joining(","));
	}


	public void createCSVOKInput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
		List<String[]> dataLines = new ArrayList<>();
		int i=0;
		int days=0;
		dataLines.add(new String[] 
				{ "COGNOME E NOME", "DATA NASCITA", "INDIRIZZO COMPLETO", "NUMERO CARTA IDENTITA", "DATA DI SCADENZA C.I.", "C.A.P.", "INDIRIZZO", "ETA_", "SIGLA SESSO (M/F)", "DATA CARTA IDENTITA", "CODICE FISCALE", "CODICE POSIZIONE", "ANAGRAFICA" });
		while (i < 5) {
			if (i == 0)	days = -1;
			if (i == 1)	days = 7;
			if (i == 2)	days = 30;
			if (i == 3)	days = 60;
			if (i == 4)	days = 90;
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			LocalDateTime d = myClock.now().plusDays(days).toLocalDateTime();
			String dueDate = d.format(formatter);
			i++;
		dataLines.add(new String[] 
		  { "CognomeYYY NomeXXX", "19/12/1930", "TRAVERSA AL N 296 DI CORSO ALCIDE DE GASPERI 11", "AO9997054",  dueDate, "70125", "90", "M", "07/06/2011", "BTTNLN30T19A662C", "10" });
		}
		dataLines.stream()
        .map(this::convertToCSVInput)
        .forEach(pw::println);
	    }
	    return;
	}
	
	
	void createCSVOKOutput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    PrintWriter pw = new PrintWriter(csvOutputFile);
		List<String[]> dataLines = new ArrayList<>();
		int i=0;
		int days=0;
		dataLines.add(new String[] 
				  { "tax_code", "expedition_date","due_date","full_name", "identity_card_number",  "release_date", "forewarning" });
		while (i < 5) {
			if (i == 0)	days = -1;
			if (i == 1)	days = 7;
			if (i == 2)	days = 30;
			if (i == 3)	days = 60;
			if (i == 4)	days = 90;
			OffsetDateTime zonedDateTime = myClock.now().plusDays(days);
			String d = zonedDateTime.toLocalDateTime().toString();
			i++;
		dataLines.add(new String[] 
				{ "BTTNLN30T19A662C", "$expedition_date", d.toString(), "CognomeYYY NomeXXX", "AO9997054", "2011-06-07" ,  String.valueOf(days)  });
		}
		dataLines.stream()
        .map(this::convertToCSVOutput)
        .forEach(pw::println);
		pw.close();
	    return;
	}
	
	// csv di input e output del test testNewExpirationCSV_ErrorInCSVOK
	void createErrorInCSVOKInput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    PrintWriter pw = new PrintWriter(csvOutputFile);
		List<String[]> dataLines = new ArrayList<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDateTime d = myClock.now().plusDays(7).toLocalDateTime();
		String dueDate = d.format(formatter);

		dataLines.add(new String[] 
				{ "COGNOME E NOME", "DATA NASCITA", "INDIRIZZO COMPLETO", "NUMERO CARTA IDENTITA", "DATA DI SCADENZA C.I.", "C.A.P.", "INDIRIZZO", "ETA_", "SIGLA SESSO (M/F)", "DATA CARTA IDENTITA", "CODICE FISCALE", "CODICE POSIZIONE", "ANAGRAFICA" });
		dataLines.add(new String[] 
				  { "CognomeYYY NomeXXX", "19-12-1930", "TRAVERSA AL N 296 DI CORSO ALCIDE DE GASPERI 11", "AO9997054",  dueDate, "70125", String.valueOf(7), "M", "07/06/2011", "BTTNLN30T19A662C", "10" });
		dataLines.add(new String[] 
				  { "CognomeYYY NomeXXX", "19/12/1930", "TRAVERSA AL N 296 DI CORSO ALCIDE DE GASPERI 11", "AO9997054",  myClock.now().plusDays(7).toString(), "70125", String.valueOf(7), "M", "07/06/2011", "BTTNLN30T19A662C", "10" });
		dataLines.add(new String[] 
				  { "CognomeYYY NomeXXX", "19/12/1930", "TRAVERSA AL N 296 DI CORSO ALCIDE DE GASPERI 11", "AO9997054",  dueDate, "70125", String.valueOf(7), "M","07-06-2011","BTTNLN30T19A662C", "10" });
		dataLines.add(new String[] 
				  { "CognomeYYY NomeXXX", "19/12/1930", "TRAVERSA AL N 296 DI CORSO ALCIDE DE GASPERI 11", "AO9997054",  dueDate , "70125", String.valueOf(7), "M", "07/06/2011", "BTTNLN30T19A662C1", "10" });
		dataLines.stream()
        .map(this::convertToCSVInput)
        .forEach(pw::println);
	    pw.close();
	    return;
	}

	
	void createErrorInCSVOKOutput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    PrintWriter pw = new PrintWriter(csvOutputFile);
		List<String[]> dataLines = new ArrayList<>();
		String dueDate = myClock.now().plusDays(7).toLocalDateTime().toString();

		dataLines.add(new String[] 
				  { "tax_code", "expedition_date","due_date","full_name", "identity_card_number",  "release_date", "forewarning" });
		dataLines.add(new String[] 
				{ "BTTNLN30T19A662C", "$expedition_date", dueDate, "CognomeYYY NomeXXX", "AO9997054", "2011-06-07", String.valueOf(7) });
		dataLines.stream()
        .map(this::convertToCSVOutput)
        .forEach(pw::println);
	    pw.close();
	    return;
	}

	
	// csv di input e output del test testNewExpirationCSV_ErrorInCSVOK
	void createNewExpirationCSV_VuotoOKInput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    PrintWriter pw = new PrintWriter(csvOutputFile);
		List<String[]> dataLines = new ArrayList<>();
		dataLines.add(new String[] 
				{ "COGNOME E NOME", "DATA NASCITA", "INDIRIZZO COMPLETO", "NUMERO CARTA IDENTITA", "DATA DI SCADENZA C.I.", "C.A.P.", "INDIRIZZO", "ETA_", "SIGLA SESSO (M/F)", "DATA CARTA IDENTITA", "CODICE FISCALE", "CODICE POSIZIONE", "ANAGRAFICA" });
		dataLines.stream()
        .map(this::convertToCSVInput)
        .forEach(pw::println);
	    pw.close();
	    return;
	}

	void createNewExpirationCSV_VuotoOKOutput(File csvOutputFile) throws IOException {
		csvOutputFile.createNewFile();
	    PrintWriter pw = new PrintWriter(csvOutputFile);
	    List<String[]> dataLines = new ArrayList<>();
		dataLines.add(new String[] 
				  { "tax_code", "expedition_date","due_date","full_name", "identity_card_number",  "release_date", "forewarning" });
		dataLines.stream()
        .map(this::convertToCSVOutput)
        .forEach(pw::println);
	    pw.close();
	    return;
	}



}
