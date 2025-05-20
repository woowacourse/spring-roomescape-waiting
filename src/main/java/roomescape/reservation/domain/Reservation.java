package roomescape.reservation.domain;

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
import roomescape.member.domain.Member;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private TimeSlot timeSlot;

    @ManyToOne
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    private Reservation(final Long id, final Member member, final LocalDate date, final TimeSlot timeSlot,
                        final Theme theme) {
        validateNull(member, date, timeSlot, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.timeSlot = timeSlot;
        this.theme = theme;
    }

    public Reservation() {
    }

    public static Reservation of(final Long id, final Member member, final LocalDate date,
                                 final TimeSlot timeSlot, final Theme theme) {
        return new Reservation(id, member, date, timeSlot, theme);
    }

    public static Reservation createWithoutId(final Member member, final LocalDate date,
                                              final TimeSlot timeSlot, final Theme theme) {
        return new Reservation(null, member, date, timeSlot, theme);
    }

    private static void validateNull(Member member, LocalDate date, TimeSlot timeSlot, Theme theme) {
        if (member == null) {
            throw new ArgumentNullException("member");
        }
        if (date == null) {
            throw new ArgumentNullException("date");
        }
        if (timeSlot == null) {
            throw new ArgumentNullException("reservationTime");
        }
        if (theme == null) {
            throw new ArgumentNullException("theme");
        }
    }

    public void validateDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(date, timeSlot.getStartAt());
        if (LocalDateTime.now().isAfter(dateTime)) {
            throw new PastDateTimeReservationException();
        }
    }

    public Reservation withId(Long id) {
        return new Reservation(id, member, date, timeSlot, theme);
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

    public TimeSlot getReservationTime() {
        return timeSlot;
    }

    public Theme getTheme() {
        return theme;
    }
}
