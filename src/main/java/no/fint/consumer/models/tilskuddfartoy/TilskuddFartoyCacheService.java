package no.fint.consumer.models.tilskuddfartoy;

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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource;
import no.fint.model.kultur.kulturminnevern.KulturminnevernActions;

@Slf4j
@Service
@ConditionalOnProperty(name = "fint.consumer.cache.disabled.tilskuddfartoy", havingValue = "false", matchIfMissing = true)
public class TilskuddFartoyCacheService extends CacheService<TilskuddFartoyResource> {

    public static final String MODEL = TilskuddFartoy.class.getSimpleName().toLowerCase();

    @Value("${fint.consumer.compatibility.fintresource:true}")
    private boolean checkFintResourceCompatibility;

    @Autowired
    private FintResourceCompatibility fintResourceCompatibility;

    @Autowired
    private ConsumerEventUtil consumerEventUtil;

    @Autowired
    private ConsumerProps props;

    @Autowired
    private TilskuddFartoyLinker linker;

    private JavaType javaType;

    private ObjectMapper objectMapper;

    public TilskuddFartoyCacheService() {
        super(MODEL, KulturminnevernActions.GET_ALL_TILSKUDDFARTOY, KulturminnevernActions.UPDATE_TILSKUDDFARTOY);
        objectMapper = new ObjectMapper();
        javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, TilskuddFartoyResource.class);
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @PostConstruct
    public void init() {
        props.getAssets().forEach(this::createCache);
    }

    @Scheduled(initialDelayString = Constants.CACHE_INITIALDELAY_TILSKUDDFARTOY, fixedRateString = Constants.CACHE_FIXEDRATE_TILSKUDDFARTOY)
    public void populateCacheAll() {
        props.getAssets().forEach(this::populateCache);
    }

    public void rebuildCache(String orgId) {
		flush(orgId);
		populateCache(orgId);
	}

    private void populateCache(String orgId) {
		log.info("Populating TilskuddFartoy cache for {}", orgId);
        Event event = new Event(orgId, Constants.COMPONENT, KulturminnevernActions.GET_ALL_TILSKUDDFARTOY, Constants.CACHE_SERVICE);
        consumerEventUtil.send(event);
    }


    public Optional<TilskuddFartoyResource> getTilskuddFartoyBySoknadsnummer(String orgId, String soknadsnummer) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(TilskuddFartoyResource::getSoknadsnummer)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(soknadsnummer))
                .orElse(false));
    }

    public Optional<TilskuddFartoyResource> getTilskuddFartoyByMappeId(String orgId, String mappeId) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(TilskuddFartoyResource::getMappeId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(mappeId))
                .orElse(false));
    }

    public Optional<TilskuddFartoyResource> getTilskuddFartoyBySystemId(String orgId, String systemId) {
        return getOne(orgId, (resource) -> Optional
                .ofNullable(resource)
                .map(TilskuddFartoyResource::getSystemId)
                .map(Identifikator::getIdentifikatorverdi)
                .map(_id -> _id.equals(systemId))
                .orElse(false));
    }


	@Override
    public void onAction(Event event) {
        List<TilskuddFartoyResource> data;
        if (checkFintResourceCompatibility && fintResourceCompatibility.isFintResourceData(event.getData())) {
            log.info("Compatibility: Converting FintResource<TilskuddFartoyResource> to TilskuddFartoyResource ...");
            data = fintResourceCompatibility.convertResourceData(event.getData(), TilskuddFartoyResource.class);
        } else {
            data = objectMapper.convertValue(event.getData(), javaType);
        }
        data.forEach(linker::mapLinks);
        if (KulturminnevernActions.valueOf(event.getAction()) == KulturminnevernActions.UPDATE_TILSKUDDFARTOY) {
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
