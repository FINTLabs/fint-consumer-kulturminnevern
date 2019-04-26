package no.fint.consumer.models.dispensasjonautomatiskfredakulturminne;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

import no.fint.cache.CacheService;
import no.fint.consumer.config.Constants;
import no.fint.consumer.config.ConsumerProps;
import no.fint.consumer.event.ConsumerEventUtil;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.felles.kompleksedatatyper.Identifikator;
import no.fint.relations.FintResourceCompatibility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import no.fint.model.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminne;
import no.fint.model.resource.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminneResource;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;

@Slf4j
@Service
public class DispensasjonAutomatiskFredaKulturminneCacheService extends CacheService<DispensasjonAutomatiskFredaKulturminneResource> {

    public static final String MODEL = DispensasjonAutomatiskFredaKulturminne.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private DispensasjonAutomatiskFredaKulturminneLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public DispensasjonAutomatiskFredaKulturminneCacheService() {
        super(MODEL, KulturminnevernActions.GET_ALL_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, DispensasjonAutomatiskFredaKulturminneResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    //@Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, fixedRateString = Constants.CACHE_FIXEDRATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    private void populateCache(String orgId) {
		log.info("Populating DispensasjonAutomatiskFredaKulturminne cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_ALL_DISPENSASJONAUTOMATISKFREDAKULTURMINNE, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<DispensasjonAutomatiskFredaKulturminneResource> getDispensasjonAutomatiskFredaKulturminneBySoknadsnummer(String orgId, String soknadsnummer) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(DispensasjonAutomatiskFredaKulturminneResource::getSoknadsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(soknadsnummer))
                .orElse(false));
    }

    public Optional<DispensasjonAutomatiskFredaKulturminneResource> getDispensasjonAutomatiskFredaKulturminneByMappeId(String orgId, String mappeId) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(DispensasjonAutomatiskFredaKulturminneResource::getMappeId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(mappeId))
                .orElse(false));
    }

    public Optional<DispensasjonAutomatiskFredaKulturminneResource> getDispensasjonAutomatiskFredaKulturminneBySystemId(String orgId, String systemId) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(DispensasjonAutomatiskFredaKulturminneResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(systemId))
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<DispensasjonAutomatiskFredaKulturminneResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<DispensasjonAutomatiskFredaKulturminneResource> to DispensasjonAutomatiskFredaKulturminneResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), DispensasjonAutomatiskFredaKulturminneResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(linker::mapLinks);
        if (KulturminnevernActions.valueOf(event.getAction()) == KulturminnevernActions.UPDATE_DISPENSASJONAUTOMATISKFREDAKULTURMINNE) {
            if (event.getResponseStatus() == ResponseStatus.ACCEPTED || event.getResponseStatus() == ResponseStatus.CONFLICT) {
                add(event.getOrgId(), data);
                log.info("Added {} elements to cache for {}", data.size(), event.getOrgId());
            } else {
                log.debug("Ignoring payload for {} with response status {}", event.getOrgId(), event.getResponseStatus());
            }
        } else {
            update(event.getOrgId(), data);
            log.info("Updated cache for {} with {} elements", event.getOrgId(), data.size());
        }
    }
}
