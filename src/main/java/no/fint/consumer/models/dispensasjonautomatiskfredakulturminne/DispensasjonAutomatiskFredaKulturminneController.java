package no.fint.consumer.models.dispensasjonautomatiskfredakulturminne;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;

import no.fint.audit.FintAuditService;

import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.exceptions.*;
import no.fint.consumer.status.StatusCache;
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

import javax.naming.NameNotFoundException;

import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResources;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;

@Slf4j
@Api(tags = {"DispensasjonAutomatiskFredaKulturminne"})
@CrossOrigin
@RestController
@RequestMapping(name = "DispensasjonAutomatiskFredaKulturminne", value = RestEndpoints.DISPENSASJONAUTOMATISKFREDAKULTURMINNE, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class DispensasjonAutomatiskFredaKulturminneController {

    @Autowired
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

    @GetMapping("/last-updated")
    public Map<String, String> getLastUpdated(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        String lastUpdated = Long.toString(cacheService.getLastUpdated(orgId));
        return ImmutableMap.of("lastUpdated", lastUpdated);
    }

    @GetMapping("/cache/size")
     public ImmutableMap<String, Integer> getCacheSize(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        return ImmutableMap.of("size", cacheService.getAll(orgId).size());
    }

    @PostMapping("/cache/rebuild")
    public void rebuildCache(@RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        cacheService.rebuildCache(orgId);
    }

    @GetMapping
    public DispensasjonAutomatiskFredaKulturminneResources getDispensasjonAutomatiskFredaKulturminne(
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(required = false) Long sinceTimeStamp) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("OrgId: {}, Client: {}", orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_ALL_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
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
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("Soknadsnummer: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("soknadsnummer/" + id);
        fintAuditService.audit(event);

        fintAuditService.audit(event, Status.CACHE);

        Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneBySoknadsnummer(orgId, id);

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));
    }

    @GetMapping("/mappeid/{id:.+}")
    public DispensasjonAutomatiskFredaKulturminneResource getDispensasjonAutomatiskFredaKulturminneByMappeId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("MappeId: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("mappeid/" + id);
        fintAuditService.audit(event);

        fintAuditService.audit(event, Status.CACHE);

        Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneByMappeId(orgId, id);

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));
    }

    @GetMapping("/systemid/{id:.+}")
    public DispensasjonAutomatiskFredaKulturminneResource getDispensasjonAutomatiskFredaKulturminneBySystemId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("SystemId: {}, OrgId: {}, Client: {}", id, orgId, client);

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, client);
        event.setQuery("systemid/" + id);
        fintAuditService.audit(event);

        fintAuditService.audit(event, Status.CACHE);

        Optional<DispensasjonAutomatiskFredaKulturminneResource> dispensasjonautomatiskfredakulturminne = cacheService.getDispensasjonAutomatiskFredaKulturminneBySystemId(orgId, id);

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return dispensasjonautomatiskfredakulturminne.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));
    }



    @GetMapping("/status/{id}")
    public ResponseEntity getStatus(
            @PathVariable String id,
            @RequestHeader(HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(HeaderConstants.CLIENT) String client) {
        log.debug("/status/{} for {} from {}", id, orgId, client);
        if (!statusCache.containsKey(id)) {
            return ResponseEntity.notFound().build();
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
        List<DispensasjonAutomatiskFredaKulturminneResource> result;
        switch (event.getResponseStatus()) {
            case ACCEPTED:
                if (event.getOperation() == Operation.VALIDATE) {
                    fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                    return ResponseEntity.ok(event.getResponse());
                }
                result = objectMapper.convertValue(event.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, DispensasjonAutomatiskFredaKulturminneResource.class));
                URI location = UriComponentsBuilder.fromUriString(linker.getSelfHref(result.get(0))).build().toUri();
                event.setMessage(location.toString());
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
            case ERROR:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(event.getResponse());
            case CONFLICT:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                result = objectMapper.convertValue(event.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, DispensasjonAutomatiskFredaKulturminneResource.class));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(linker.toResources(result));
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
        event.setOperation(Operation.CREATE);
        if (validate) {
            event.setQuery("VALIDATE");
            event.setOperation(Operation.VALIDATE);
        }
        fintAuditService.audit(event);

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
    @ExceptionHandler(UpdateEntityMismatchException.class)
    public ResponseEntity handleUpdateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(e);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity handleEntityNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e);
    }

    @ExceptionHandler(CreateEntityMismatchException.class)
    public ResponseEntity handleCreateEntityMismatch(Exception e) {
        return ResponseEntity.badRequest().body(e);
    }

    @ExceptionHandler(EntityFoundException.class)
    public ResponseEntity handleEntityFound(Exception e) {
        return ResponseEntity.status(HttpStatus.FOUND).body(e);
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity handleNameNotFound(Exception e) {
        return ResponseEntity.badRequest().body(e);
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity handleUnkownHost(Exception e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e);
    }

}

