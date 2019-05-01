package no.fint.consumer.config;

import com.google.common.collect.ImmutableMap;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.administrasjon.arkiv.*;
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
                .put(Saksstatus.class.getName(), "/administrasjon/arkiv/saksstatus")
                .put(TilknyttetRegistreringSom.class.getName(), "/administrasjon/arkiv/tilknyttetregistreringsom")
                .put(DokumentStatus.class.getName(), "/administrasjon/arkiv/dokumentstatus")
                .put(Dokumentfil.class.getName(), "/administrasjon/arkiv/dokumentfil")
                .put(KorrespondansepartType.class.getName(), "/administrasjon/arkiv/korrespondanseparttype")
                .put(JournalpostType.class.getName(), "/administrasjon/arkiv/journalposttype")
                .put(JournalStatus.class.getName(), "/administrasjon/arkiv/journalstatus")
                .build();
    }

}
