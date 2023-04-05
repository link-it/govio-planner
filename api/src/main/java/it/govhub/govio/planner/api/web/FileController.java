package it.govhub.govio.planner.api.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import it.govhub.govio.planner.api.beans.ExpirationFile;
import it.govhub.govio.planner.api.beans.GovioFileList;
import it.govhub.govio.planner.api.config.GovioPlannerRoles;
import it.govhub.govio.planner.api.spec.FileApi;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.security.services.SecurityService;

@V1RestController
public class FileController implements FileApi {
	
	// Questo deve far parte del contesto ed essere caricato dall'applicazione all'avvio?
	@Value("${govio-planner.organization}")
	String organizationTaxCode;
	
	@Value("${govio-planner.service-instance}")
	Long serviceInstanceId;
	
	@Value("${govio-planner.expiration-basis}")
	List<Long> cadenzaSpedizione;
	
	@Value("${govio-planner.filerepository.path:/var/govio-planner/csv}")
	Path fileRepositoryPath;
	
	@Autowired
	SecurityService authService;
	
	Logger logger = LoggerFactory.getLogger(FileController.class);

	@Override
	public ResponseEntity<ExpirationFile> uploadExpirations(MultipartFile file) {
		
		this.authService.expectAnyRole(GovioPlannerRoles.GOVIOPLANNER_OPERATOR);
		
		HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
		
		// Leggo il body multipart ed estraggo nomeFile e stream di input
		String sourceFilename = null;
		FileItemStream itemStream = null;
		
		try {
			FileItemIterator iterStream = new ServletFileUpload().getItemIterator(request);
			
			// Leggo il body multipart ed estraggo il file,
			// NOTA: la clausola del while DEVE essere scritta in questo ordine. Lo stream corrente viene invalidato
			// quando viene chiamato iterStream.hasNext.
			// Per lo short-circuit dell'&&, una volta trovato l'elemento multipart necessario, usciamo dal while
			// senza chiamare iterStream.hasNext
			logger.debug("Reading Multipart Elements..");
			while (sourceFilename == null && iterStream.hasNext()) {
			    itemStream = iterStream.next();
			    logger.debug("Found element: {}", itemStream.getFieldName());
			    
			    if (itemStream.isFormField()) {
			    	logger.debug("Skipping multipart form field {}", itemStream.getFieldName());
			    } else {
				    sourceFilename = RequestUtils.readFilenameFromHeaders(itemStream.getHeaders());
			    }
			}
		} catch (Exception e) {
			throw new InternalException(e);
		}
		
    	if (StringUtils.isEmpty(sourceFilename)) {
    		throw new BadRequestException("E' necessario indicare il filename nello header Content-Disposition del blocco multipart del file.\ne.g: [Content-Disposition: form-data; name=\"file\"; filename=\"file.csv\"] ");
    	}
    	
    	Path destPath = this.fileRepositoryPath;
				/*.resolve(instance.getOrganization().getId().toString())
				.resolve(instance.getService().getId().toString());*/
	
		File destDir = destPath.toFile();
		destDir.mkdirs();
		
		if (!destDir.isDirectory()) {
			logger.error("Impossibile creare la directory per conservare i files: {}", destDir);
			throw new RuntimeException("Non è stato possibile creare la directory per conservare i files");
		}
		
		String destFilename = sourceFilename + "-" + this.organizationTaxCode + "-" + this.serviceInstanceId + Instant.now();
		Path destFile =  destPath.resolve(destFilename);
		
		logger.info("Streaming uploaded csv [{}] to [{}]", sourceFilename, destFile);
		
		if (destFile.toFile().exists()) {
			throw new InternalException("File: " + destFile + "già esistente!");
		}
		
		long size;
		try(InputStream stream=itemStream.openStream()){
			size = Files.copy(stream, destFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new InternalException(e);	
		}
		
		/*GovioFileEntity file = GovioFileEntity.builder()
			.creationDate(OffsetDateTime.now())
			.govauthUser(SecurityService.getPrincipal())
			.location(destFile)
			.name(sourceFilename)
			.serviceInstance(instance)
			.status(GovioFileEntity.Status.CREATED)
			.size(size)
			.build();*/
		
//		return this.fileRepo.save(file);

    	/**/
//    	GovioFileEntity created = this.fileService.uploadCSV(serviceInstance, sourceFilename, itemStream);
    	
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<GovioFileList> listGovioFiles(Integer limit, Long offset, String q, Long expirationFileId) {
		// TODO Auto-generated method stub
		return null;
	}


}
