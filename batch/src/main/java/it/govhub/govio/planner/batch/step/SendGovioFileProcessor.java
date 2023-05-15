package it.govhub.govio.planner.batch.step;

import java.net.URL;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity;
import it.govhub.govio.planner.batch.entity.GovioFileProducedEntity.Status;
import it.govhub.govio.planner.batch.exception.BackendGovioRuntimeException;
import it.govhub.govio.planner.batch.exception.ShouldSkipException;
import it.govhub.govio.v1.api.FileApi;

@Component
public class SendGovioFileProcessor implements ItemProcessor<GovioFileProducedEntity, GovioFileProducedEntity> {
	
	private Logger logger = LoggerFactory.getLogger(SendGovioFileProcessor.class);

	@Autowired
	private FileApi govioFileClient;
	
	@Value("${planner.govio.client-debugging:false")
	private boolean debugging;
	
	@Value("${planner.govio.service-instance}")
	private Long serviceInstanceId = 1L;
	
	@Value( "${planner.govio.retry-after-default:3600}" )
	protected int defaultRetryTimer;
	
    @Value( "${planner.govio.retry-after-max:10000}" )
	protected int maxRetryTimer;
	
    @Value("${planner.govio.principal}")
    protected String apiKey;
    
    @Value("${planner.govio.upload-url}")
    protected URL uploadUrl;
	
	@PostConstruct
	private void prepareClient() {
		govioFileClient.getApiClient().setBasePath(uploadUrl.toString());
		govioFileClient.getApiClient().setApiKey(apiKey);
		govioFileClient.getApiClient().setDebugging(debugging);
	}

	@Override
	public GovioFileProducedEntity process(GovioFileProducedEntity item) throws Exception {
		logger.info("Spedizione messaggio [{}] a GovIo", item.getId());
		try {
			govioFileClient.uploadFile(serviceInstanceId, item.getLocation().toFile());					
		} catch (HttpClientErrorException e) {
			handleUploadException(e);
		}
		
		item.setStatus(Status.SENT);
		return item;
	}
	
	
	private void handleUploadException(HttpClientErrorException e) {
		switch (e.getStatusCode()) {
		
		case UNPROCESSABLE_ENTITY:
			logger.warn("Messaggio [{}] già inviato in un'altra run del job...");
			break;
		case UNAUTHORIZED:
		case FORBIDDEN:
		case NOT_FOUND:
			// Questi sono inaspettati, fermo il job.
			logErrorResponse(e);
			throw(e);
		case BAD_REQUEST:
			// Questi errori dovrebbero essere "colpa" nostra, riptere è inutile, skippo l'elemento e loggo un errore
			logErrorResponse(e);
			throw new ShouldSkipException(e);
		case TOO_MANY_REQUESTS:
			HttpHeaders responseHeaders = e.getResponseHeaders();
			String value    =  responseHeaders.getFirst("Retry-After");
			int sleepTime =  value == null ? defaultRetryTimer  : Integer.parseInt(value);
			sleepTime       = Math.min(sleepTime, maxRetryTimer);
			sleep(sleepTime);
			logger.error("Ricevuta eccezione 429, aspettato {} prima di riprovare",sleepTime );
			throw new BackendGovioRuntimeException(e);
		case INTERNAL_SERVER_ERROR:
		case BAD_GATEWAY:
		case SERVICE_UNAVAILABLE:
			// SuI 5xx ripeto l'item
			throw new BackendGovioRuntimeException(e);
		default:
			logErrorResponse(e);
			throw e;
		}
	}
	
	private void sleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	protected void logErrorResponse(HttpStatusCodeException e) {
		if(e instanceof HttpServerErrorException) {
			logger.error("Ricevuto server error da BackendIO: {}", e.getMessage());
		}
		else {
			logger.debug("Ricevuto client error da BackendIO: {}", e.getMessage());
		}
		logger.debug("HTTP Status Code: {}", e.getRawStatusCode());
		logger.debug("Status Text: {}", e.getStatusText());
		logger.debug("HTTP Headers: {}", e.getResponseHeaders());
		logger.debug("Response Body: {}", e.getResponseBodyAsString());	
	}

}
