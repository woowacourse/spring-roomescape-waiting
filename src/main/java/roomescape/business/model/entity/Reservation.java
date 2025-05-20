package roomescape.business.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.*;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import roomescape.business.model.vo.Status;

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
    @Enumerated(EnumType.STRING)
    private Status status;
    private final LocalDateTime createdAt;


    protected Reservation() {
        id = Id.issue();
        createdAt = LocalDateTime.now();
    }

    public static Reservation create(final User user, final LocalDate date, final ReservationTime time, final Theme theme, final Status status, final LocalDateTime createdAt) {
        return new Reservation(Id.issue(), user, ReservationDate.create(date), time, theme, status, createdAt.truncatedTo(ChronoUnit.MILLIS));
    }

    public static Reservation restore(final String id, final User user, final LocalDate date, final ReservationTime time, final Theme theme, final Status status, final LocalDateTime createdAt) {
        return new Reservation(Id.create(id), user, ReservationDate.restore(date), time, theme, status, createdAt.truncatedTo(ChronoUnit.MILLIS));
    }

    public boolean isSameReserver(final String userId) {
        return user.isSameUser(userId);
    }
}
