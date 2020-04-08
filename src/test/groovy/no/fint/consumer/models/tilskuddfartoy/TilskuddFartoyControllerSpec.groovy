package no.fint.consumer.models.tilskuddfartoy

import com.fasterxml.jackson.databind.ObjectMapper
import no.fint.audit.FintAuditService
import no.fint.consumer.config.ConsumerProps
import no.fint.consumer.event.ConsumerEventUtil
import no.fint.consumer.status.StatusCache
import no.fint.model.resource.kultur.kulturminnevern.TilskuddFartoyResource
import no.fint.test.utils.MockMvcSpecification
import org.hamcrest.CoreMatchers
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc

/*
 * This specification asserts that the manually-added mappings for /mappeid/{year}/{sequence}
 * are present.  If the controller is replaced with the generated version, these mappings will
 * be deleted.
 * If you get here due to an unexpected build failure, go back to you Git history and locate the
 * @GetMapping and @PutMapping for /mappeid/{ar}/{sekvensnummer}
 */
class TilskuddFartoyControllerSpec extends MockMvcSpecification {

    private MockMvc mockMvc
    private TilskuddFartoyCacheService cacheService
    private TilskuddFartoyController controller
    private TilskuddFartoyLinker linker
    private ConsumerProps props
    private FintAuditService auditService
    private ConsumerEventUtil consumerEventUtil
    private StatusCache statusCache

    void setup() {
        cacheService = Mock()
        props = Mock()
        auditService = Mock()
        linker = Mock()
        consumerEventUtil = Mock()
        statusCache = Mock()
        controller = new TilskuddFartoyController(
                cacheService: cacheService,
                linker: linker,
                props: props,
                fintAuditService: auditService,
                consumerEventUtil: consumerEventUtil,
                statusCache: statusCache,
                objectMapper: new ObjectMapper())
        mockMvc = standaloneSetup(controller)
    }

    def "Verify that GET by mappeId works"() {
        when:
        def response = mockMvc.perform(
                get('/tilskuddfartoy/mappeid/2020/42')
                        .header('x-org-id', 'test.org')
                        .header('x-client', 'Spock'))

        then:
        response.andExpect(status().is2xxSuccessful())
        1 * cacheService.getTilskuddFartoyByMappeId('test.org', '2020/42') >> Optional.of(new TilskuddFartoyResource())
        1 * linker.toResource(_ as TilskuddFartoyResource) >> new TilskuddFartoyResource()
    }

    def "Verify that PUT by mappeId works"() {
        when:
        def response = mockMvc.perform(
                put('/tilskuddfartoy/mappeid/2020/22')
                        .header('x-org-id', 'test.org')
                        .header('x-client', 'Spock')
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("{}")
        )

        then:
        response
                .andExpect(status().is2xxSuccessful())
                .andExpect(header().string('Location', CoreMatchers.containsString('/status/')))
        1 * linker.self() >> '/tilskuddfartoy/'
    }
}
