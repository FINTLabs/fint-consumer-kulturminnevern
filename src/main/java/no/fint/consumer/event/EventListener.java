package no.fint.consumer.event;

import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.cache.CacheService;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.status.StatusCache;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.events.FintEventListener;
import no.fint.events.FintEvents;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventListener implements FintEventListener {

    @Autowired(required = false)
    private List<CacheService> cacheServices;
    
	@Autowired
	private FintEvents fintEvents;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    StatusCache statusCache;

    @Autowired
    private ConsumerProps props;

    @PostConstruct
    public void init() {
        fintEvents.registerUpstreamSystemListener(this);
        if (cacheServices == null)
            cacheServices = Collections.emptyList();
    	for (String orgId : props.getAssets()) {
    		fintEvents.registerUpstreamListener(orgId, this);
    	}
    	log.info("Upstream listeners registered.");
    }

	@Override
	public void accept(Event event) {
        log.debug("Received event: {}", event);
        log.trace("Event data: {}", event.getData());
        if (event.isRegisterOrgId()) {
            if (props.getAssets().add(event.getOrgId())) {
                log.info("Registering orgId {} for {}", event.getOrgId(), event.getClient());
                fintEvents.registerUpstreamListener(event.getOrgId(), this);
                cacheServices.forEach(c -> c.createCache(event.getOrgId()));
            }
            return;
        } else if (event.isHealthCheck()) {
            log.debug("Ignoring health check.");
            return;
        }
        if (statusCache.containsKey(event.getCorrId())) {
            statusCache.put(event.getCorrId(), event);
        }
        if (event.getOperation() == Operation.VALIDATE) {
            log.debug("Ignoring validation event.");
            return;
        }
        if (event.getResponseStatus() == ResponseStatus.REJECTED || event.getResponseStatus() == ResponseStatus.ERROR) {
            log.debug("Ignoring response status {}", event.getResponseStatus());
            return;
        }
        String action = event.getAction();
        List<CacheService> supportedCacheServices = cacheServices.stream().filter(cacheService -> cacheService.supportsAction(action)).collect(Collectors.toList());
        if (supportedCacheServices.size() > 0) {
            try {
                supportedCacheServices.forEach(cacheService -> cacheService.onAction(event));
                fintAuditService.audit(event, Status.CACHE);
            } catch (Exception e) {
                log.warn("Error handling event {} {}", event.getOrgId(), event.getCorrId(), e);
                event.setMessage(ExceptionUtils.getStackTrace(e));
                fintAuditService.audit(event, Status.ERROR);
            }
        } else {
            event.setMessage("No Cache Service supports action");
            fintAuditService.audit(event, Status.ERROR);
            log.warn("Unhandled event: {}", event);
        }
    }
	
}
