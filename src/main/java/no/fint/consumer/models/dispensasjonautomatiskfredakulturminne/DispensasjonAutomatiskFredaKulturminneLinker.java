package no.fint.consumer.models.dispensasjonautomatiskfredakulturminne;

import no.fint.model.resource.Link;
import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResources;
import no.fint.relations.FintLinker;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Objects.isNull;
import static org.springframework.util.StringUtils.isEmpty;


@Component
public class DispensasjonAutomatiskFredaKulturminneLinker extends FintLinker<DispensasjonAutomatiskFredaKulturminneResource> {

    public DispensasjonAutomatiskFredaKulturminneLinker() {
        super(DispensasjonAutomatiskFredaKulturminneResource.class);
    }

    public void mapLinks(DispensasjonAutomatiskFredaKulturminneResource resource) {
        super.mapLinks(resource);
    }

    @Override
    public DispensasjonAutomatiskFredaKulturminneResources toResources(Collection<DispensasjonAutomatiskFredaKulturminneResource> collection) {
        DispensasjonAutomatiskFredaKulturminneResources resources = new DispensasjonAutomatiskFredaKulturminneResources();
        collection.stream().map(this::toResource).forEach(resources::addResource);
        resources.addSelf(Link.with(self()));
        return resources;
    }

    @Override
    public String getSelfHref(DispensasjonAutomatiskFredaKulturminneResource dispensasjonautomatiskfredakulturminne) {
        if (!isNull(dispensasjonautomatiskfredakulturminne.getSoknadsnummer()) && !isEmpty(dispensasjonautomatiskfredakulturminne.getSoknadsnummer().getIdentifikatorverdi())) {
            return createHrefWithId(dispensasjonautomatiskfredakulturminne.getSoknadsnummer().getIdentifikatorverdi(), "soknadsnummer");
        }
        if (!isNull(dispensasjonautomatiskfredakulturminne.getMappeId()) && !isEmpty(dispensasjonautomatiskfredakulturminne.getMappeId().getIdentifikatorverdi())) {
            return createHrefWithId(dispensasjonautomatiskfredakulturminne.getMappeId().getIdentifikatorverdi(), "mappeid");
        }
        if (!isNull(dispensasjonautomatiskfredakulturminne.getSystemId()) && !isEmpty(dispensasjonautomatiskfredakulturminne.getSystemId().getIdentifikatorverdi())) {
            return createHrefWithId(dispensasjonautomatiskfredakulturminne.getSystemId().getIdentifikatorverdi(), "systemid");
        }
        
        return null;
    }
    
}

