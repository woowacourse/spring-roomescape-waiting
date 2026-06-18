package roomescape.event.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import roomescape.common.DomainEvent;

@Component
@RequiredArgsConstructor
public class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishEvents(Iterable<DomainEvent> events) {
        events.forEach(applicationEventPublisher::publishEvent);
    }
}
