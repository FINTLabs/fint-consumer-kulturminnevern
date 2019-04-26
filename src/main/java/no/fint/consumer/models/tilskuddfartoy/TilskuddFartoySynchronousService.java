package no.fint.consumer.models.tilskuddfartoy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import no.fint.audit.FintAuditService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.event.model.Operation;
import no.fint.event.model.ResponseStatus;
import no.fint.model.administrasjon.arkiv.ArkivActions;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;
import no.fint.model.resource.administrasjon.arkiv.JournalpostResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TilskuddFartoySynchronousService {

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private FintAuditService fintAuditService;

    @Autowired
    private ObjectMapper objectMapper;

    public ResponseEntity getTilskuddFartoy(String id, String orgId, String client) {
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_TILSKUDDFARTOY, client);
        event.setQuery(id);
        event.setOperation(Operation.READ);
        fintAuditService.audit(event);

        Optional<Event> response = consumerEventUtil.sendSync(event);

        if (response.isPresent()) {
            if (response.get().getData().size() == 1) {
                TilskuddFartoyResource tilskuddFartoyResource = objectMapper.convertValue(response.get().getData().get(0), TilskuddFartoyResource.class);
                return ResponseEntity.ok(tilskuddFartoyResource);
            }
            if (response.get().getResponse().getResponseStatus() == ResponseStatus.ERROR) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ImmutableMap.of("message", response.get().getMessage()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    public ResponseEntity searchTilskuddFartoyByTitle(String title, String orgId, String client) {

        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_TILSKUDDFARTOY, client);
        event.setQuery(title);
        event.setOperation(Operation.READ);
        fintAuditService.audit(event);

        Optional<Event> response = consumerEventUtil.sendSync(event);

        if (response.isPresent()) {
            if (response.get().getData().size()  > 0) {
                List<TilskuddFartoyResource> tilskuddFartoyResources = objectMapper.convertValue(response.get().getData(), new TypeReference<List<TilskuddFartoyResource>>(){});
                return ResponseEntity.ok(tilskuddFartoyResources);
            }
            if (response.get().getResponse().getResponseStatus() == ResponseStatus.ERROR) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ImmutableMap.of("message", response.get().getMessage()));
            }
        }
        return ResponseEntity.notFound().build();
    }

    /*
    public ResponseEntity getRegistrering(String id, String orgId, String client) {
        Event event = new Event(orgId, Constants.COMPONENT, ArkivActions.GET_JOURNALPOST, client);
        event.setQuery(id);
        event.setOperation(Operation.READ);
        fintAuditService.audit(event);

        Optional<Event> response = consumerEventUtil.sendSync(event);

        if (response.isPresent()) {
            JournalpostResource journalpostResource = objectMapper.convertValue(response.get().getData().get(0), JournalpostResource.class);
            return ResponseEntity.ok(journalpostResource);
        }

        return ResponseEntity.notFound().build();
    }
    */

}
