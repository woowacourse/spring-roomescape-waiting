package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Temporal(TemporalType.DATE)
    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    public Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateTheme(theme);

        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("[ERROR] 회원 정보는 반드시 입력해야 합니다.");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("[ERROR] 예약 날짜는 반드시 입력해야 합니다. 예시) YYYY-MM-DD");
        }
    }

    private void validateReservationTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("[ERROR] 예약 시간을 반드시 입력해야 합니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("[ERROR] 예약 시간을 반드시 입력해야 합니다.");
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
