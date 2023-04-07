package it.govhub.govio.planner.api.web;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import it.govhub.govio.planner.api.assemblers.ExpirationFileAssembler;
import it.govhub.govio.planner.api.beans.ExpirationFile;
import it.govhub.govio.planner.api.beans.ExpirationFileEmbeds;
import it.govhub.govio.planner.api.beans.ExpirationFileList;
import it.govhub.govio.planner.api.beans.GovioFile;
import it.govhub.govio.planner.api.beans.GovioFileList;
import it.govhub.govio.planner.api.config.GovioPlannerRoles;
import it.govhub.govio.planner.api.entity.ExpirationCIEFileEntity;
import it.govhub.govio.planner.api.entity.ExpirationCIEFileEntity_;
import it.govhub.govio.planner.api.filters.ExpirationCIEFileFilters;
import it.govhub.govio.planner.api.messages.ExpirationFileMessages;
import it.govhub.govio.planner.api.repository.ExpirationCIEFileEntityRepository;
import it.govhub.govio.planner.api.services.PlannerFileService;
import it.govhub.govio.planner.api.spec.FileApi;
import it.govhub.govregistry.commons.api.beans.UserList;
import it.govhub.govregistry.commons.config.V1RestController;
import it.govhub.govregistry.commons.entity.UserEntity;
import it.govhub.govregistry.commons.exception.BadRequestException;
import it.govhub.govregistry.commons.exception.InternalException;
import it.govhub.govregistry.commons.exception.ResourceNotFoundException;
import it.govhub.govregistry.commons.utils.LimitOffsetPageRequest;
import it.govhub.govregistry.commons.utils.ListaUtils;
import it.govhub.govregistry.commons.utils.RequestUtils;
import it.govhub.govregistry.readops.api.repository.UserFilters;
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

	@Autowired
	SecurityService authService;
	
	@Autowired
	PlannerFileService fileService;
	
	@Autowired
	ExpirationCIEFileEntityRepository fileRepo;
	
	@Autowired
	ExpirationFileMessages fileMessages;
	
	@Autowired
	ExpirationFileAssembler fileAssembler;
	
	Logger logger = LoggerFactory.getLogger(FileController.class);

	@Override
	public ResponseEntity<ExpirationFile> uploadExpirationsFile(String planId, MultipartFile file) {
		
		// Per adesso il planID è hard-coded, perchè unico, Nel caso di più installazioni, diventerà un parametro.
		// Potrei fare una whitelist ora...
		planId = "bari-cie-exp";
		
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
		} catch(FileUploadException e) {
			throw new BadRequestException(e);
		}catch (Exception e) {
			throw new InternalException(e);
		}
		
    	if (StringUtils.isEmpty(sourceFilename)) {
    		throw new BadRequestException("E' necessario indicare il filename nello header Content-Disposition del blocco multipart del file.\ne.g: [Content-Disposition: form-data; name=\"file\"; filename=\"file.csv\"] ");
    	}
    	
    	ExpirationCIEFileEntity created = this.fileService.uploadCSV(planId, sourceFilename, itemStream);
    	var ret = this.fileAssembler.toEmbeddedModel(created);
    	
		return ResponseEntity.status(201).body(ret);
	}



	@Override
	public ResponseEntity<Resource> downloadExpirationsFile(Long expirationFileId) {
		
		this.authService.expectAnyRole(GovioPlannerRoles.GOVIOPLANNER_OPERATOR);

		ExpirationCIEFileEntity file = this.fileRepo.findById(expirationFileId)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(expirationFileId)));
		
		Path path = file.getLocation();
		
		FileInputStream stream;
		try {
			stream = new FileInputStream(path.toFile());
		} catch (FileNotFoundException e) {
			throw new InternalException(e);
		}

		InputStreamResource fileStream = new InputStreamResource(stream);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentLength(file.getSize());
		return new ResponseEntity<>(fileStream, headers, HttpStatus.OK); 
		
	}
	

	@Override
	public ResponseEntity<ExpirationFile> readExpirationsInfo(Long expirationFileId) {
		
		this.authService.expectAnyRole(GovioPlannerRoles.GOVIOPLANNER_OPERATOR);
		
		ExpirationCIEFileEntity file = this.fileRepo.findById(expirationFileId)
				.orElseThrow( () -> new ResourceNotFoundException(this.fileMessages.idNotFound(expirationFileId)));

		return ResponseEntity.ok(this.fileAssembler.toEmbeddedModel(file));
	}
	

	@Override
	public ResponseEntity<ExpirationFileList> listExpirationFiles( Direction sortDirection, Integer limit,	Long offset,String q, List<ExpirationFileEmbeds> embed) {

		this.authService.expectAnyRole(GovioPlannerRoles.GOVIOPLANNER_OPERATOR);

		LimitOffsetPageRequest pageRequest = new LimitOffsetPageRequest(offset, limit,Sort.by(sortDirection, ExpirationCIEFileEntity_.CREATION_DATE));
		
		Page<ExpirationCIEFileEntity> files = this.fileRepo.findAll(ExpirationCIEFileFilters.empty(), pageRequest.pageable);
		
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes()).getRequest();
		
		ExpirationFileList ret = ListaUtils.buildPaginatedList(files, pageRequest.limit, curRequest, new ExpirationFileList());
		
		for (ExpirationCIEFileEntity file : files) {
			ret.addItemsItem(this.fileAssembler.toModel(file));
		}
		
		return ResponseEntity.ok(ret);

	}




	@Override
	public ResponseEntity<Resource> downloadGovioFileInfo(Long id) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ResponseEntity<GovioFileList> listGovioFiles( Integer limit, Long offset, String q, Long expirationFileId) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public ResponseEntity<GovioFile> readGovioFileInfo(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
