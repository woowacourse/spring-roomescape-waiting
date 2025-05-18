package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import roomescape.domain.theme.Theme;
import roomescape.domain.timeslot.TimeSlot;
import roomescape.domain.user.User;

@EqualsAndHashCode(of = {"id"})
@Getter
@Accessors(fluent = true)
@ToString
@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private User user;
    @Embedded
    private ReservationDateTime dateTime;
    @ManyToOne
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation(final Long id, final User user, final ReservationDateTime dateTime, final Theme theme, final ReservationStatus status) {
        this.id = id;
        this.user = user;
        this.dateTime = dateTime;
        this.theme = theme;
        this.status = status;
    }

    public Reservation(final User user, final LocalDate date, final TimeSlot timeSlot, final Theme theme) {
        this(null, user, ReservationDateTime.forReserve(date, timeSlot), theme, ReservationStatus.RESERVED);
    }

    protected Reservation() {
    }
}

