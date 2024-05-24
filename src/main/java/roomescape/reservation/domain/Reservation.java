package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        validateNotNull(date, reservationTime, theme, member);
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(Long id, Reservation reservation) {
        this(id, reservation.date, reservation.reservationTime, reservation.theme, reservation.member);
    }

    public Reservation(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public Reservation(Waiting waiting) {
        this(waiting.getDate(), waiting.getReservationTime(), waiting.getTheme(), waiting.getMember());
    }

    private void validateNotNull(LocalDate date, ReservationTime reservationTime, Theme theme, Member member) {
        if (date == null || reservationTime == null || theme == null || member == null) {
            throw new IllegalArgumentException("Reservation의 date, reservationTime, theme, member는 null일 수 없습니다.");
        }
    }

    public boolean isBeforeNow() {
        return reservationTime.isBeforeNow(date);
    }

    public boolean isBetweenInclusive(LocalDate dateFrom, LocalDate dateTo) {
        return !date.isBefore(dateFrom) && !date.isAfter(dateTo);
    }

    public boolean isSameMember(Member member) {
        return Objects.equals(this.member, member);
    }

    public boolean isNotDeletableMemeber(Member member) {
        return !(member.isAdmin()) && !(Objects.equals(this.member, member));
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDate(DateTimeFormatter formatter) {
        return date.format(formatter);
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
