package roomescape.common;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDomain {

    private final List<DomainEvent> events = new ArrayList<>();

    protected void addEvent(DomainEvent event) {
        events.add(event);
    }

    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulledEvents = List.copyOf(events);
        events.clear();
        return pulledEvents;
    }
}
