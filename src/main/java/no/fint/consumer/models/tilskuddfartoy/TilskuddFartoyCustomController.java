package no.fint.consumer.models.tilskuddfartoy;

import no.fint.consumer.utils.RestEndpoints;
import no.fint.event.model.HeaderConstants;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.relations.FintRelationsMediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = RestEndpoints.TILSKUDDFARTOY, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class TilskuddFartoyCustomController {
    @Autowired
    private TilskuddFartoyController tilskuddFartoyController;

    @GetMapping("/mappeid/{ar}/{sekvensnummer}")
    public TilskuddFartoyResource getTilskuddFartoyByMappeArSekvensnummer(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        return tilskuddFartoyController.getTilskuddFartoyByMappeId(ar + "/" + sekvensnummer, orgId, client);
    }

    @PutMapping("/mappeid/{ar}/{sekvensnummer}")
    public ResponseEntity putTilskuddFartoyByMappeArSekvensnummer(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestBody TilskuddFartoyResource body) {
        return tilskuddFartoyController.putTilskuddFartoyByMappeId(ar + "/" + sekvensnummer, orgId, client, body);
    }

}
