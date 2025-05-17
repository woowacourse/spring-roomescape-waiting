package roomescape.business.model.entity;

import jakarta.persistence.*;
import lombok.*;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.time.LocalDate;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
@Table(name = "reservation")
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

    protected Reservation() {
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
