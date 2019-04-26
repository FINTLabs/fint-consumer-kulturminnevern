package no.fint.consumer.models.tilskuddfartoy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.consumer.exceptions.CreateEntityMismatchException;
import no.fint.consumer.exceptions.EntityFoundException;
import no.fint.consumer.exceptions.EntityNotFoundException;
import no.fint.consumer.exceptions.UpdateEntityMismatchException;
import no.fint.consumer.status.StatusCache;
import no.fint.consumer.utils.QueryUtils;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.event.model.*;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.relations.FintRelationsMediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.naming.NameNotFoundException;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("Duplicates")
@Slf4j
@Api(tags = {"TilskuddFartoy"})
@CrossOrigin
@RestController
@RequestMapping(name = "TilskuddFartoy", value = RestEndpoints.TILSKUDDFARTOY, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class TilskuddFartoyController {

    @Autowired
    private TilskuddFartoyCacheService cacheService;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private TilskuddFartoyLinker linker;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private StatusCache statusCache;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TilskuddFartoySynchronousService tilskuddFartoySynchronousService;

    /*
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

    */

    @GetMapping
    public ResponseEntity getTilskuddFartoy(
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestParam(required = true) String title,
            @RequestParam(required = false, defaultValue = "10") int maxResult,
            HttpServletRequest request) {
        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("Title: {}, PageSize: {}, OrgId: {}, Client: {}", title, maxResult, orgId, client);

        return tilskuddFartoySynchronousService.searchTilskuddFartoyByTitle(QueryUtils.createQuery(request.getQueryString()), orgId, client);
    }


    @GetMapping("/soknadsnummer/{id:.+}")
    public TilskuddFartoyResource getTilskuddFartoyBySoknadsnummer(
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

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_TILSKUDDFARTOY, client);
        event.setQuery("soknadsnummer/" + id);
        fintAuditService.audit(event);

        fintAuditService.audit(event, Status.CACHE);

        Optional<TilskuddFartoyResource> tilskuddfartoy = cacheService.getTilskuddFartoyBySoknadsnummer(orgId, id);

        fintAuditService.audit(event, Status.CACHE_RESPONSE, Status.SENT_TO_CLIENT);

        return tilskuddfartoy.map(linker::toResource).orElseThrow(() -> new EntityNotFoundException(id));
    }

    @GetMapping("/mappeid/{ar}/{sekvensnummer}")
    public ResponseEntity getTilskuddFartoyByMappeId(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {

        if (props.isOverrideOrgId() || orgId == null) {
            orgId = props.getDefaultOrgId();
        }
        if (client == null) {
            client = props.getDefaultClient();
        }
        log.debug("MappeId: {}/{}, OrgId: {}, Client: {}", ar, sekvensnummer, orgId, client);

        return tilskuddFartoySynchronousService.getTilskuddFartoy(String.format("mappeid/%s/%s", ar, sekvensnummer), orgId, client);
    }

    /*
    @GetMapping("/mappeid/{ar}/{sekvensnummer}/registrering/systemid/{id}")
    public ResponseEntity getTilskuddFartoyByMappeId(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) {

        return tilskuddFartoySynchronousService.getRegistrering(id, orgId, client);
    }
    */



    @GetMapping("/systemid/{id:.+}")
    public ResponseEntity getTilskuddFartoyBySystemId(
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

        return tilskuddFartoySynchronousService.getTilskuddFartoy(String.format("systemid/%s", id), orgId, client);
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
            return ResponseEntity.badRequest().body(new EventResponse() {
                {
                    setMessage("Invalid OrgId");
                }
            });
        }
        if (event.getResponseStatus() == null) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        }
        List<TilskuddFartoyResource> result;
        switch (event.getResponseStatus()) {
            case ACCEPTED:
                if (event.getOperation() == Operation.VALIDATE) {
                    fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                    return ResponseEntity.ok(event.getResponse());
                }
                result = objectMapper.convertValue(event.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, TilskuddFartoyResource.class));
                URI location = UriComponentsBuilder.fromUriString(linker.getSelfHref(result.get(0))).build().toUri();
                event.setMessage(location.toString());
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.status(HttpStatus.SEE_OTHER).location(location).build();
            case ERROR:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(event.getResponse());
            case CONFLICT:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                result = objectMapper.convertValue(event.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, TilskuddFartoyResource.class));
                return ResponseEntity.status(HttpStatus.CONFLICT).body(linker.toResources(result));
            case REJECTED:
                fintAuditService.audit(event, Status.SENT_TO_CLIENT);
                return ResponseEntity.badRequest().body(event.getResponse());
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(event.getResponse());
    }

    @PostMapping
    public ResponseEntity postTilskuddFartoy(
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody TilskuddFartoyResource body
            //@RequestParam(name = "validate", required = false) boolean validate
    ) {
        log.debug("postTilskuddFartoy, OrgId: {}, Client: {}", orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_TILSKUDDFARTOY, client);
        event.addObject(objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).convertValue(body, Map.class));
        event.setOperation(Operation.CREATE);

        fintAuditService.audit(event);

        consumerEventUtil.send(event);

        statusCache.put(event.getCorrId(), event);

        URI location = UriComponentsBuilder.fromUriString(linker.self()).path("status/{id}").buildAndExpand(event.getCorrId()).toUri();
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).build();
    }


    @PutMapping("/soknadsnummer/{id:.+}")
    public ResponseEntity putTilskuddFartoyBySoknadsnummer(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody TilskuddFartoyResource body
    ) {
        log.debug("putTilskuddFartoyBySoknadsnummer {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_TILSKUDDFARTOY, client);
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
    public ResponseEntity putTilskuddFartoyByMappeId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody TilskuddFartoyResource body
    ) {
        log.debug("putTilskuddFartoyByMappeId {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_TILSKUDDFARTOY, client);
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
    public ResponseEntity putTilskuddFartoyBySystemId(
            @PathVariable String id,
            @RequestHeader(name = HeaderConstants.ORG_ID) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT) String client,
            @RequestBody TilskuddFartoyResource body
    ) {
        log.debug("putTilskuddFartoyBySystemId {}, OrgId: {}, Client: {}", id, orgId, client);
        log.trace("Body: {}", body);
        linker.mapLinks(body);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.UPDATE_TILSKUDDFARTOY, client);
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

