package roomescape.event.publisher;

import roomescape.common.DomainEvent;

public interface EventPublisher {

    void publishEvents(Iterable<DomainEvent> events);
}
