package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.ArgumentNullException;
import roomescape.exception.PastDateTimeReservationException;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    private Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime reservationTime,
                        final Theme theme) {
        validateNull(member, date, reservationTime, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    protected Reservation() {
    }

    public static Reservation of(final Long id, final Member member, final LocalDate date,
                                 final ReservationTime reservationTime, final Theme theme) {
        return new Reservation(id, member, date, reservationTime, theme);
    }

    public static Reservation createWithoutId(final Member member, final LocalDate date,
                                              final ReservationTime reservationTime, final Theme theme) {
        return new Reservation(null, member, date, reservationTime, theme);
    }

    private static void validateNull(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (member == null) {
            throw new ArgumentNullException("member");
        }
        if (date == null) {
            throw new ArgumentNullException("date");
        }
        if (reservationTime == null) {
            throw new ArgumentNullException("reservationTime");
        }
        if (theme == null) {
            throw new ArgumentNullException("theme");
        }
    }

    public void validateDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (LocalDateTime.now().isAfter(dateTime)) {
            throw new PastDateTimeReservationException();
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }
}
