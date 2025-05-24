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
public class Waiting {

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

    @Column(nullable = false)
    private LocalDateTime createAt;

    private Waiting(final Long id, final Member member, final LocalDate date, final ReservationTime reservationTime,
                    final Theme theme, final LocalDateTime createAt) {
        validateNull(member, date, reservationTime, theme, createAt);
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.createAt = createAt;
    }

    protected Waiting() {
    }

    public static Waiting of(final Long id, final Member member, final LocalDate date,
                             final ReservationTime reservationTime, final Theme theme, final LocalDateTime createAt) {
        return new Waiting(id, member, date, reservationTime, theme, createAt);
    }

    public static Waiting createWithoutId(final Member member, final LocalDate date,
                                          final ReservationTime reservationTime, final Theme theme,
                                          final LocalDateTime createAt) {
        return new Waiting(null, member, date, reservationTime, theme, createAt);
    }

    private static void validateNull(Member member, LocalDate date, ReservationTime reservationTime, Theme theme,
                                     LocalDateTime createAt) {
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
        if (createAt == null) {
            throw new ArgumentNullException("createAt");
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
