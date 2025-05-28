package roomescape.business.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.business.model.vo.Id;

import java.time.LocalDateTime;

@ToString
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Entity
public class Reservation {

    @EmbeddedId
    private final Id id = Id.issue();
    @ManyToOne
    private final User user;
    @ManyToOne
    private final ReservationSlot slot;
    @Column(updatable = false)
    private final LocalDateTime createdAt;

    public Reservation(final User user, final ReservationSlot slot) {
        this.user = user;
        this.slot = slot;
        this.createdAt = LocalDateTime.now();
        slot.getDate().validateFresh();
        slot.addReservation(this);
    }

    public boolean isSameReserver(final String userId) {
        return user.isSameUser(userId);
    }
}
