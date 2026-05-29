package roomescape.reservation.domain;

import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"user", "slot"})
public class Waiting {

    private final Long id;
    private final User user;
    private final ReservationSlot slot;

    @Builder
    public Waiting(Long id, User user, ReservationSlot slot) {
        this.id = id;
        this.user = Objects.requireNonNull(user);
        this.slot = Objects.requireNonNull(slot);
    }

    public Waiting withId(Long generatedId) {
        return Waiting.builder()
                .id(generatedId)
                .user(this.user)
                .slot(this.slot)
                .build();
    }
}
