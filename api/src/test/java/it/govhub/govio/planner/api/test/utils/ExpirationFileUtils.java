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
package it.govhub.govio.planner.api.test.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

import it.govhub.govio.planner.api.entity.ExpirationFileEntity;
import it.govhub.govio.planner.api.entity.ExpirationFileEntity.Status;
import it.govhub.govio.planner.api.entity.GovioPlannerFileEntity;
import it.govhub.govregistry.commons.entity.UserEntity;

public class ExpirationFileUtils {
	
	public static ExpirationFileEntity buildFile(Path fileRepositoryPath, String i, UserEntity user, String planId) throws IOException {
		Path destPath = fileRepositoryPath.resolve("expiration_files");
		
		File destDir = destPath.toFile();
    	destDir.mkdirs();
		
		File file = new File(destDir, i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++) {
			file1writer.write("CA"+String.format("%06d", x)+"AA,2022-12-31,CA"+String.format("%06d", x)+"AA,2022-12-31\n");
		}
		file1writer.close();
    	
    	Path destFile =  destPath.resolve(file.getName());

    	ExpirationFileEntity govioFile1 = ExpirationFileEntity.builder()
				.creationDate(OffsetDateTime.now())
				.size(destFile.toFile().length())
				.location(destFile)
				.name(file.getName())
				.status(Status.CREATED)
				.uploaderUser(user)
				.planId(planId)
				.build();

		return govioFile1;
	}
	
	
	public static GovioPlannerFileEntity buildGovIOFile(Path fileRepositoryPath, String i, ExpirationFileEntity expirationFile) throws IOException {
		Path destPath = fileRepositoryPath.resolve("govio_files");
		
		File destDir = destPath.toFile();
    	destDir.mkdirs();
		
		File file = new File(destDir, i+".csv");
		FileWriter file1writer = new FileWriter(file);
		file1writer.write("Testata\n");
		for(int x=0;x<100;x++) {
			file1writer.write("CA"+String.format("%06d", x)+"AA,2022-12-31,CA"+String.format("%06d", x)+"AA,2022-12-31\n");
		}
		file1writer.close();
    	
    	Path destFile =  destPath.resolve(file.getName());

    	GovioPlannerFileEntity govioFile1 = GovioPlannerFileEntity.builder() 
				.creationDate(OffsetDateTime.now())
				.size(destFile.toFile().length())
				.location(destFile)
				.name(file.getName())
				.status(it.govhub.govio.planner.api.entity.GovioPlannerFileEntity.Status.CREATED)
				.expirationFile(expirationFile)
				.messageCount(100L)
				.build();

		return govioFile1;
	}
	
	public static String createApiKey() {
		return UUID.randomUUID().toString().replace("-", "");
	}

}
