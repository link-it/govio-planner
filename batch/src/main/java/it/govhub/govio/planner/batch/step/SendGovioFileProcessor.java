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
import it.govhub.govio.planner.batch.exception.ShouldRetryException;
import it.govhub.govio.planner.batch.exception.ShouldSkipException;
import it.govhub.govio.v1.api.FileApi;

@Component
public class SendGovioFileProcessor implements ItemProcessor<GovioFileProducedEntity, GovioFileProducedEntity> {
	
	private Logger logger = LoggerFactory.getLogger(SendGovioFileProcessor.class);

	@Autowired
	private FileApi govioFileClient;
	
	@Value("${planner.govio.client-debugging:false}")
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
		logger.info("Spedizione CSV di Messaggi [{}] a GovIo", item.getId());
		try {
			govioFileClient.uploadFile(serviceInstanceId, item.getLocation().toFile());					
		} catch (HttpClientErrorException e) {
			handleUploadException(e, item);
		}
		
		item.setStatus(Status.SENT);
		return item;
	}
	
	
	private void handleUploadException(HttpClientErrorException e, GovioFileProducedEntity item) {
		switch (e.getStatusCode()) {
		
		case UNPROCESSABLE_ENTITY:
			logger.warn("CSV di Messaggi [{}] già inviato in un'altra run del job...", item.getId());
			break;
		case UNAUTHORIZED:
		case FORBIDDEN:
		case NOT_FOUND:
			// Questi sono inaspettati, fermo il job.
			logErrorResponse(e);
			throw e;
		case BAD_REQUEST:
			// Queso  errori dovrebbero essere "colpa" nostra, riptere è inutile, skippo l'elemento e loggo un errore
			logErrorResponse(e);
			logger.error("Ricevuta BadRequest per CSV di Messaggi [{}]t, skippo il file.", item.getId());
			throw new ShouldSkipException(e);
		case TOO_MANY_REQUESTS:
			int sleepTime = getRetryAfter(e.getResponseHeaders());
			sleep(sleepTime);
			logger.debug("Ricevuta TooManyRequest 429 per CSV di Messaggi [{}], aspettato {} prima di riprovare",item.getId(), sleepTime );
			throw new ShouldRetryException(e);
		case INTERNAL_SERVER_ERROR:
		case BAD_GATEWAY:
		case SERVICE_UNAVAILABLE:
			// SuI 5xx ripeto l'item
			logger.warn("Ricevuto errore {} per CSV di Messaggi [{}], riprovo.", e.getStatusCode(), item.getId());
			throw new ShouldRetryException(e);
		default:
			logErrorResponse(e);
			throw e;
		}
	}
	
	private int getRetryAfter(HttpHeaders headers) {
		String value    =  headers.getFirst("Retry-After");
		int sleepTime =  value == null ? defaultRetryTimer  : Integer.parseInt(value);
		sleepTime       = Math.min(sleepTime, maxRetryTimer);
		return sleepTime;
	}
	
	private void sleep(int sleepTime) {
		try { Thread.sleep(sleepTime); }
		catch ( InterruptedException e) {	throw new RuntimeException(e); 	}
	}
	
	
	protected void logErrorResponse(HttpStatusCodeException e) {
		if(e instanceof HttpServerErrorException) {
			logger.error("Ricevuto server error da GovIO: {}", e.getMessage());
		}
		else {
			logger.error("Ricevuto client error da GovIO: {}", e.getMessage());
		}
		logger.debug("HTTP Status Code: {}", e.getRawStatusCode());
		logger.debug("Status Text: {}", e.getStatusText());
		logger.debug("HTTP Headers: {}", e.getResponseHeaders());
		logger.debug("Response Body: {}", e.getResponseBodyAsString());	
	}

}
