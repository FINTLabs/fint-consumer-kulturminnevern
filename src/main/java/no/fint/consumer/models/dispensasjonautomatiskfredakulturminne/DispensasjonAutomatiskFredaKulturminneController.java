package no.fint.consumer.models.dispensasjonautomatiskfredakulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import no.fint.audit.FintAuditService;

import no.fint.cache.exceptions.*;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.event.SynchronousEvents;
import no.fint.consumer.exceptions.*;
import no.fint.consumer.status.StatusCache;
import no.fint.consumer.utils.EventResponses;
import no.fint.consumer.utils.RestEndpoints;

import no.fint.event.model.*;

import no.fint.relations.FintRelationsMediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.UnknownHostException;
import java.net.URI;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResources;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;

@Slf4j
@Api(tags = {"DispensasjonAutomatiskFredaKulturminne"})
@CrossOrigin
@RestController
@RequestMapping(name = "DispensasjonAutomatiskFredaKulturminne", value = RestEndpoints.DISPENSASJONAUTOMATISKFREDAKULTURMINNE, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class DispensasjonAutomatiskFredaKulturminneController {

    @Autowired(required = false)
    private DispensasjonAutomatiskFredaKulturminneCacheService cacheService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private DispensasjonAutomatiskFredaKulturminneLinker linker;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private StatusCache statusCache;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SynchronousEvents synchronousEvents;

    @GetMapping("/last-updated")
    public Map<String, String> getLastUpdated(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (cacheService == null) {
            throw new CacheDisabledException("DispensasjonAutomatiskFredaKulturminne cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        String lastUpdated = Long.toString(cacheService.getLastUpdated(orgId));
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

    @GetMapping("/cache/size")
     public ImmutableMap<String, Integer> getCacheSize(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (cacheService == null) {
            throw new CacheDisabledException("DispensasjonAutomatiskFredaKulturminne cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        return ImmutableMap.of("size", cacheService.getCacheSize(orgId));
    }

    @GetMapping
    public DispensasjonAutomatiskFredaKulturminneResources getDispensasjonAutomatiskFredaKulturminne(
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(required = false) Long sinceTimeStamp) {
        if (cacheService == null) {
            throw new CacheDisabledException("DispensasjonAutomatiskFredaKulturminne cache is disabled.");
        }
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("OrgId: {}, Client: {}", orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_ALL_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setOperation(Operation.READ);
        fintAuditService.audit(event);
        fintAuditService.audit(event, Status.CACHE);

        List<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne;
        if (sinceTimeStamp == null) {
            dispensasjonautomatiskfredakulturminne = cacheService.getAll(orgId);
        } else {
            dispensasjonautomatiskfredakulturminne = cacheService.getAll(orgId, sinceTimeStamp);
        }

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return linker.toResources(dispensasjonautomatiskfredakulturminne);
    }


    @GetMapping("/soknadsnummer/{id:.+}")
    public DispensasjonAutomatiskFredaKulturminneResource getDispensasjonAutomatiskFredaKulturminneBySoknadsnummer(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("soknadsnummer: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setOperation(Operation.READ);
        event.setQuery("soknadsnummer/" + id);

        if (cacheService != null) {
            fintAuditService.audit(event);
            fintAuditService.audit(event, Status.CACHE);

            Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneBySoknadsnummer(orgId, id);

            fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

            return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));

        } else {
            BlockingQueue<Event> queue = synchronousEvents.register(event);
            consumerEventUtil.send(event);

            Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));

            if (response.getData() == null ||
                    response.getData().isEmpty()) throw new EntityNotFoundException(id);

            DispensasjonAutomatiskFredaKulturminneResource dispensasjonautomatiskfredakulturminne = objectMapper.convertValue(response.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);

            fintAuditService.audit(response, Status.SENT_TO_CLIENT);

            return linker.toResource(dispensasjonautomatiskfredakulturminne);
        }    
    }

    @GetMapping("/mappeid/{id:.+}")
    public DispensasjonAutomatiskFredaKulturminneResource getDispensasjonAutomatiskFredaKulturminneByMappeId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("mappeId: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setOperation(Operation.READ);
        event.setQuery("mappeId/" + id);

        if (cacheService != null) {
            fintAuditService.audit(event);
            fintAuditService.audit(event, Status.CACHE);

            Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneByMappeId(orgId, id);

            fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

            return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));

        } else {
            BlockingQueue<Event> queue = synchronousEvents.register(event);
            consumerEventUtil.send(event);

            Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));

            if (response.getData() == null ||
                    response.getData().isEmpty()) throw new EntityNotFoundException(id);

            DispensasjonAutomatiskFredaKulturminneResource dispensasjonautomatiskfredakulturminne = objectMapper.convertValue(response.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);

            fintAuditService.audit(response, Status.SENT_TO_CLIENT);

            return linker.toResource(dispensasjonautomatiskfredakulturminne);
        }    
    }

    @GetMapping("/systemid/{id:.+}")
    public DispensasjonAutomatiskFredaKulturminneResource getDispensasjonAutomatiskFredaKulturminneBySystemId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("systemId: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setOperation(Operation.READ);
        event.setQuery("systemId/" + id);

        if (cacheService != null) {
            fintAuditService.audit(event);
            fintAuditService.audit(event, Status.CACHE);

            Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneBySystemId(orgId, id);

            fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

            return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));

        } else {
            BlockingQueue<Event> queue = synchronousEvents.register(event);
            consumerEventUtil.send(event);

            Event response = EventResponses.handle(queue.poll(5, TimeUnit.MINUTES));

            if (response.getData() == null ||
                    response.getData().isEmpty()) throw new EntityNotFoundException(id);

            DispensasjonAutomatiskFredaKulturminneResource dispensasjonautomatiskfredakulturminne = objectMapper.convertValue(response.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);

            fintAuditService.audit(response, Status.SENT_TO_CLIENT);

            return linker.toResource(dispensasjonautomatiskfredakulturminne);
        }    
    }



    // Writable class
    @GetMapping("/status/{id}")
    public ResponseEntity getStatus(
            @PathVariable String id,
            @RequestHeader(HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(HeaderConstants.CLIENT) String client) {
        log.debug("/status/{} for {} from {}", id, orgId, client);
        if (!statusCache.containsKey(id)) {
            return ResponseEntity.status(HttpStatus.GONE).build();
        }
        Event event = statusCache.get(id);
        log.debug("Event: {}", event);
        log.trace("Data: {}", event.getData());
        if (!event.getOrgId().equals(orgId)) {
            return ResponseEntity.badRequest().body(new EventResponse() { { setMessage("Invalid OrgId"); } } );
        }
        if (event.getResponseStatus() == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        DispensasjonAutomatiskFredaKulturminneResource result;
        switch (event.getResponseStatus()) {
            case ACCEPTED:
                if (event.getOperation() == Operation.VALIDATE) {
                    fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                    return ResponseEntity.ok(event.getResponse());
                }
                result = objectMapper.convertValue(event.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);
                URI location = UriComponentsBuilder.fromUriString(linker.getSelfHref(result)).build().toUri();
                event.setMessage(location.toString());
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                if (props.isUseCreated())
                    return ResponseEntity.created(location).body(linker.toResource(result));
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).body(linker.toResource(result));
            case ERROR:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(event.getResponse());
            case CONFLICT:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                result = objectMapper.convertValue(event.getData().get(0), DispensasjonAutomatiskFredaKulturminneResource.class);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(linker.toResource(result));
            case REJECTED:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.badRequest().body(event.getResponse());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event.getResponse());
    }

    @PostMapping
    public ResponseEntity postDispensasjonAutomatiskFredaKulturminne(
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody DispensasjonAutomatiskFredaKulturminneResource body,
            @RequestParam(name = "validate", required = false) boolean validate
    ) {
        log.debug("postDispensasjonAutomatiskFredaKulturminne, Validate: {}, OrgId: {}, Client: {}", validate, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(validate ? Operation.VALIDATE : Operation.CREATE);
        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }

  
    @PutMapping("/soknadsnummer/{id:.+}")
    public ResponseEntity putDispensasjonAutomatiskFredaKulturminneBySoknadsnummer(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody DispensasjonAutomatiskFredaKulturminneResource body
    ) {
        log.debug("putDispensasjonAutomatiskFredaKulturminneBySoknadsnummer {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("soknadsnummer/" + id);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.UPDATE);
        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }
  
    @PutMapping("/mappeid/{id:.+}")
    public ResponseEntity putDispensasjonAutomatiskFredaKulturminneByMappeId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody DispensasjonAutomatiskFredaKulturminneResource body
    ) {
        log.debug("putDispensasjonAutomatiskFredaKulturminneByMappeId {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("mappeid/" + id);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.UPDATE);
        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }
  
    @PutMapping("/systemid/{id:.+}")
    public ResponseEntity putDispensasjonAutomatiskFredaKulturminneBySystemId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody DispensasjonAutomatiskFredaKulturminneResource body
    ) {
        log.debug("putDispensasjonAutomatiskFredaKulturminneBySystemId {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("systemid/" + id);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.UPDATE);
        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }
  

    //
    // Exception handlers
    //
    @ExceptionHandler(EventResponseException.class)
    public ResponseEntity handleEventResponseException(EventResponseException e) {
        return ResponseEntity.status(e.getStatus()).body(e.getResponse());
    }

    @ExceptionHandler(UpdateEntityMismatchException.class)
    public ResponseEntity handleUpdateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(e));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CreateEntityMismatchException.class)
    public ResponseEntity handleCreateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(ErrorResponse.of(e));
    }

    @ExceptionHandler(EntityFoundException.class)
    public ResponseEntity handleEntityFound(Exception e) {
        return ResponseEntity.status(HttpStatus.FOUND).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CacheDisabledException.class)
    public ResponseEntity handleBadRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity handleUnkownHost(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

    @ExceptionHandler(CacheNotFoundException.class)
    public ResponseEntity handleCacheNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse.of(e));
    }

}

