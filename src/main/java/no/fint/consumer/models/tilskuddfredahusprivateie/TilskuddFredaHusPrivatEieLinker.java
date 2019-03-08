package no.fint.consumer.models.tilskuddfredahusprivateie;

import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;


@Component
public class TilskuddFredaHusPrivatEieLinker extends FintLinker<TilskuddFredaHusPrivatEieResource> {

    public TilskuddFredaHusPrivatEieLinker() {
        super(TilskuddFredaHusPrivatEieResource.class);
    }

    public void mapLinks(TilskuddFredaHusPrivatEieResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public TilskuddFredaHusPrivatEieResources toResources(Collection<TilskuddFredaHusPrivatEieResource> collection) {
        TilskuddFredaHusPrivatEieResources resources = new TilskuddFredaHusPrivatEieResources();
        collection.stream().map(this::toResource).forEach(resources::addResource);
        resources.addSelf(Link.with(self()));
        return resources;
    }

    @Override
    public String getSelfHref(TilskuddFredaHusPrivatEieResource tilskuddfredahusprivateie) {
        if (!isNull(tilskuddfredahusprivateie.getSoknadsnummer()) && !isEmpty(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi(), "soknadsnummer");
        }
        if (!isNull(tilskuddfredahusprivateie.getMappeId()) && !isEmpty(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi(), "mappeid");
        }
        if (!isNull(tilskuddfredahusprivateie.getSystemId()) && !isEmpty(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi(), "systemid");
        }
        
        return null;
    }
    
}

