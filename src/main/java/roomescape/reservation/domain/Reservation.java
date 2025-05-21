package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.global.exception.custom.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.member = member;
        this.theme = theme;
    }

    public static Reservation register(final Member member, final LocalDate date,
                                       final ReservationTime time, final Theme theme) {
        Reservation reservation = new Reservation(null, member, date, time, theme);
        if (reservation.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("지나간 날짜와 시간은 예약 불가합니다.");
        }
        return reservation;
    }

    protected Reservation() {
    }

    public boolean isBefore(final LocalDateTime other) {
        if (date.isBefore(other.toLocalDate())) {
            return true;
        }
        if (date.equals(other.toLocalDate())) {
            return time.isBeforeOrEqual(other.toLocalTime());
        }
        return false;
    }

    public boolean hasOwner(final Member other) {
        return this.member.equals(other);
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

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
