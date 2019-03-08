package no.fint.consumer.models.tilskuddfartoy;

import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;


@Component
public class TilskuddFartoyLinker extends FintLinker<TilskuddFartoyResource> {

    public TilskuddFartoyLinker() {
        super(TilskuddFartoyResource.class);
    }

    public void mapLinks(TilskuddFartoyResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public TilskuddFartoyResources toResources(Collection<TilskuddFartoyResource> collection) {
        TilskuddFartoyResources resources = new TilskuddFartoyResources();
        collection.stream().map(this::toResource).forEach(resources::addResource);
        resources.addSelf(Link.with(self()));
        return resources;
    }

    @Override
    public String getSelfHref(TilskuddFartoyResource tilskuddfartoy) {
        if (!isNull(tilskuddfartoy.getSoknadsnummer()) && !isEmpty(tilskuddfartoy.getSoknadsnummer().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfartoy.getSoknadsnummer().getIdentifikatorverdi(), "soknadsnummer");
        }
        if (!isNull(tilskuddfartoy.getMappeId()) && !isEmpty(tilskuddfartoy.getMappeId().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfartoy.getMappeId().getIdentifikatorverdi(), "mappeid");
        }
        if (!isNull(tilskuddfartoy.getSystemId()) && !isEmpty(tilskuddfartoy.getSystemId().getIdentifikatorverdi())) {
            return createHrefWithId(tilskuddfartoy.getSystemId().getIdentifikatorverdi(), "systemid");
        }
        
        return null;
    }
    
}

