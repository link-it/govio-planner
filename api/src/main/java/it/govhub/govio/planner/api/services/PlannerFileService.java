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
package it.govhub.govio.planner.api.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.transaction.Transactional;

import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity.Status;
import it.govhub.govio.planner.api.repository.ExpirationFileEntityRepository;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.security.services.SecurityService;

@Service
public class PlannerFileService {
	
	@Value("${planner.exp.csv-dir:/var/govio-planner/csv}")
	Path fileRepositoryPath;
	
	@Autowired
	ExpirationFileEntityRepository fileRepo;
	
	@Autowired
	SecurityService authService;
	
	Logger log = LoggerFactory.getLogger(PlannerFileService.class);
	
	
	@Transactional
	public ExpirationFileEntity uploadCSV(String planId, String sourceFilename, FileItemStream itemStream) {
		log.info("Uploading expiration file {} for Govio-Plan: {}", sourceFilename,  planId);
		
		Path destPath = this.fileRepositoryPath
    			.resolve(planId);
    	
    	File destDir = destPath.toFile();
    	destDir.mkdirs();
    	
    	if (!destDir.isDirectory()) {
    		log.error("Impossibile creare la directory per conservare i files: {}", destDir);
    		throw new InternalException("Non è stato possibile creare la directory per conservare i files");
    	}
    	
    	String destFilename  = UUID.randomUUID() + "-" + sourceFilename;
    	Path destFile =  destPath
    				.resolve(destFilename);
    	
    	log.info("Streaming uploaded csv [{}] to [{}]", sourceFilename, destFile);
    	
    	long size;
    	try(InputStream stream=itemStream.openStream()){
			size = Files.copy(stream, destFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new InternalException(e);	
		}
    	
    	ExpirationFileEntity file = ExpirationFileEntity.builder()
    			.planId(planId)
    			.creationDate(OffsetDateTime.now())
    			.uploaderUser(SecurityService.getPrincipal())
    			.location(destFile)
    			.name(sourceFilename)
    			.status(Status.CREATED)
    			.size(size)
    			.build();
    	
    	return this.fileRepo.save(file);
	}
	
}
