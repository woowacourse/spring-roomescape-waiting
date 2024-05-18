package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(Long id, Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        validate(member, date, reservationTime, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public Reservation(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        this(null, member, date, reservationTime, theme);
    }

    protected Reservation() {
    }

    private void validate(Member member, LocalDate date, ReservationTime reservationTime, Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("예약하려는 사용자를 선택해주세요.");
        }
        if (date == null) {
            throw new IllegalArgumentException("예약날짜를 선택해주세요.");
        }
        if (reservationTime == null) {
            throw new IllegalArgumentException("예약시간을 선택해주세요.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("예약하려는 테마를 선택해주세요.");
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member) && Objects.equals(date, that.date)
               && Objects.equals(reservationTime, that.reservationTime) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, reservationTime, theme);
    }
}
