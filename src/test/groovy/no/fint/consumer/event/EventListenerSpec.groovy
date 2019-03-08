package no.fint.consumer.event

import no.fint.audit.FintAuditService
import no.fint.consumer.status.StatusCache
import no.fint.event.model.Event
import no.fint.event.model.ResponseStatus
import no.fint.event.model.Status
import spock.lang.Specification

class EventListenerSpec extends Specification {
    private EventListener eventListener
    private StatusCache statusCache
    private FintAuditService fintAuditService

    void setup() {
        fintAuditService = Mock()
        statusCache = Mock()
        eventListener = new EventListener(cacheServices: [], statusCache: statusCache, fintAuditService: fintAuditService)
    }

    def "No exception is thrown when receiving event"() {
        when:
        eventListener.accept(new Event(corrId: '123', responseStatus: ResponseStatus.ACCEPTED))

        then:
        noExceptionThrown()
        1 * statusCache.containsKey(_ as String) >> false
        1 * fintAuditService.audit(_ as Event, _ as Status)
    }
}
