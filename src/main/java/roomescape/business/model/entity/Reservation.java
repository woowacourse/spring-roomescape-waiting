package roomescape.business.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
public class Reservation {

    @EmbeddedId
    private final Id id;
    @ManyToOne
    private User user;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;

    public Reservation() {
        id = Id.issue();
    }

    public static Reservation create(final User user, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(Id.issue(), user, ReservationDate.create(date), time, theme);
    }

    public static Reservation restore(final String id, final User user, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(Id.create(id), user, ReservationDate.restore(date), time, theme);
    }

    public boolean isSameReserver(final String userId) {
        return user.isSameUser(userId);
    }
}
