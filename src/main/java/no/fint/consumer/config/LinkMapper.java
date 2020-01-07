package no.fint.consumer.config;

import com.google.common.collect.ImmutableMap;
import no.fint.consumer.utils.RestEndpoints;
import no.fint.model.administrasjon.arkiv.*;
import no.fint.model.administrasjon.organisasjon.Organisasjonselement;
import no.fint.model.administrasjon.personal.Personalressurs;
import no.fint.model.kultur.kulturminnevern.DispensasjonAutomatiskFredaKulturminne;
import no.fint.model.kultur.kulturminnevern.TilskuddFartoy;
import no.fint.model.kultur.kulturminnevern.TilskuddFredaHusPrivatEie;

import java.util.Map;

public class LinkMapper {

    public static Map<String, String> linkMapper(String contextPath) {
        return ImmutableMap.<String, String>builder()
                .put(DispensasjonAutomatiskFredaKulturminne.class.getName(), contextPath + RestEndpoints.DISPENSASJONAUTOMATISKFREDAKULTURMINNE)
                .put(TilskuddFartoy.class.getName(), contextPath + RestEndpoints.TILSKUDDFARTOY)
                .put(TilskuddFredaHusPrivatEie.class.getName(), contextPath + RestEndpoints.TILSKUDDFREDAHUSPRIVATEIE)

                .put(AdministrativEnhet.class.getName(), "/administrasjon/arkiv/administrativenhet")
                .put(Arkivdel.class.getName(), "/administrasjon/arkiv/arkivdel")
                .put(Arkivressurs.class.getName(), "/administrasjon/arkiv/arkivressurs")
                .put(Autorisasjon.class.getName(), "/administrasjon/arkiv/autorisasjon")
                .put(Dokumentfil.class.getName(), "/administrasjon/arkiv/dokumentfil")
                .put(DokumentStatus.class.getName(), "/administrasjon/arkiv/dokumentstatus")
                .put(DokumentType.class.getName(), "/administrasjon/arkiv/dokumenttype")
                .put(JournalpostType.class.getName(), "/administrasjon/arkiv/journalposttype")
                .put(JournalStatus.class.getName(), "/administrasjon/arkiv/journalstatus")
                .put(Klasse.class.getName(), "/administrasjon/arkiv/klasse")
                .put(Klassifikasjonssystem.class.getName(), "/administrasjon/arkiv/klassifikasjonssystem")
                .put(Korrespondansepart.class.getName(), "/administrasjon/arkiv/korrespondansepart")
                .put(KorrespondansepartType.class.getName(), "/administrasjon/arkiv/korrespondanseparttype")
                .put(Merknadstype.class.getName(), "/administrasjon/arkiv/merknadstype")
                .put(Part.class.getName(), "/administrasjon/arkiv/part")
                .put(PartRolle.class.getName(), "/administrasjon/arkiv/partrolle")
                .put(Saksstatus.class.getName(), "/administrasjon/arkiv/saksstatus")
                .put(TilknyttetRegistreringSom.class.getName(), "/administrasjon/arkiv/tilknyttetregistreringsom")
                .put(Skjermingshjemmel.class.getName(), "/administrasjon/arkiv/skjermingshjemmel")
                .put(Tilgang.class.getName(), "/administrasjon/arkiv/tilgang")
                .put(Tilgangsrestriksjon.class.getName(), "/administrasjon/arkiv/tilgangsrestriksjon")
                .put(Variantformat.class.getName(), "/administrasjon/arkiv/variantformat")

                .put(Organisasjonselement.class.getName(), "/administrasjon/organisasjon/organisasjonselement")
                .put(Personalressurs.class.getName(), "/administrasjon/personal/personalressurs")
                .build();
    }

}
