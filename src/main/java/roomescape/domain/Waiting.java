package roomescape.domain;

import java.time.LocalDateTime;

public class Waiting {

    private Long id;
    private final LocalDateTime createdAt;
    private final Long slotId;
    private final String name;

    public Waiting(Long id, LocalDateTime createdAt, Long slotId, String name) {
        this.id = id;
        this.createdAt = createdAt;
        this.slotId = slotId;
        this.name = name;
    }

    public Waiting(LocalDateTime createdAt, Long slotId, String name) {
        this(null, createdAt, slotId, name);
    }

    public Waiting createWithId(Long id) {
        return new Waiting(id, this.createdAt, this.slotId, this.name);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getSlotId() {
        return slotId;
    }

    public String getName() {
        return name;
    }
}
