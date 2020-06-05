package no.fint.consumer.models.tilskuddfredahusprivateie;

import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResource;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFredaHusPrivatEieResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return getAllSelfHrefs(tilskuddfredahusprivateie).findFirst().orElse(null);
    }

    @Override
    public Stream<String> getAllSelfHrefs(TilskuddFredaHusPrivatEieResource tilskuddfredahusprivateie) {
        Stream.Builder<String> builder = Stream.builder();
        if (!isNull(tilskuddfredahusprivateie.getSoknadsnummer()) && !isEmpty(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi(), "soknadsnummer"));
        }
        if (!isNull(tilskuddfredahusprivateie.getMappeId()) && !isEmpty(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi(), "mappeid"));
        }
        if (!isNull(tilskuddfredahusprivateie.getSystemId()) && !isEmpty(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi())) {
            builder.add(createHrefWithId(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi(), "systemid"));
        }
        
        return builder.build();
    }

    int[] hashCodes(TilskuddFredaHusPrivatEieResource tilskuddfredahusprivateie) {
        IntStream.Builder builder = IntStream.builder();
        if (!isNull(tilskuddfredahusprivateie.getSoknadsnummer()) && !isEmpty(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi())) {
            builder.add(tilskuddfredahusprivateie.getSoknadsnummer().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(tilskuddfredahusprivateie.getMappeId()) && !isEmpty(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi())) {
            builder.add(tilskuddfredahusprivateie.getMappeId().getIdentifikatorverdi().hashCode());
        }
        if (!isNull(tilskuddfredahusprivateie.getSystemId()) && !isEmpty(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi())) {
            builder.add(tilskuddfredahusprivateie.getSystemId().getIdentifikatorverdi().hashCode());
        }
        
        return builder.build().toArray();
    }

}

