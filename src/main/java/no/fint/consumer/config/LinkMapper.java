package no.fint.consumer.config;

import com.google.common.collect.ImmutableMap;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminne;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.kultur.kulturminnevern.TilskuddFredaHusPrivatEie;

import java.util.Map;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String,String>builder()
                .put(DispensasjonAutomatiskFredaKulturminne.class.getName(), contextPath + RestEndpoints.DISPENSASJONAUTOMATISKFREDAKULTURMINNE)
                .put(TilskuddFartoy.class.getName(), contextPath + RestEndpoints.TILSKUDDFARTOY)
                .put(TilskuddFredaHusPrivatEie.class.getName(), contextPath + RestEndpoints.TILSKUDDFREDAHUSPRIVATEIE)
                /* .put(TODO,TODO) */
                .build();
    }

}
