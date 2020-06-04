package no.fint.consumer.models.tilskuddfredahusprivateie;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.event.model.HeaderConstants;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResource;
import no.fint.relations.FintRelationsMediaType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = {"TilskuddFredaHusPrivatEie"})
@CrossOrigin
@RestController
@RequestMapping(name = "TilskuddFredaHusPrivatEie", value = RestEndpoints.TILSKUDDFREDAHUSPRIVATEIE, produces = {FintRelationsMediaType.APPLICATION_HAL_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
public class TilskuddFredaHusPrivatEieCustomController {

    private final TilskuddFredaHusPrivatEieController controller;

    public TilskuddFredaHusPrivatEieCustomController(TilskuddFredaHusPrivatEieController controller) {
        this.controller = controller;
    }

    @GetMapping("/mappeid/{ar}/{sekvensnummer}")
    public TilskuddFredaHusPrivatEieResource getTilskuddFredaHusPrivatEieByMappeArSekvensnummer(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client) throws InterruptedException {
        return controller.getTilskuddFredaHusPrivatEieByMappeId(ar + "/" + sekvensnummer, orgId, client);
    }

    @PutMapping("/mappeid/{ar}/{sekvensnummer}")
    public ResponseEntity putTilskuddFredaHusPrivatEieByMappeArSekvensnummer(
            @PathVariable String ar,
            @PathVariable String sekvensnummer,
            @RequestHeader(name = HeaderConstants.ORG_ID, required = false) String orgId,
            @RequestHeader(name = HeaderConstants.CLIENT, required = false) String client,
            @RequestBody TilskuddFredaHusPrivatEieResource body) {
        return controller.putTilskuddFredaHusPrivatEieByMappeId(ar + "/" + sekvensnummer, orgId, client, body);
    }

}

