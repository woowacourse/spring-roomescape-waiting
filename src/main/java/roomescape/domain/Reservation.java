package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected Reservation() {
    }

    public Reservation(
            Long id, LocalDate date, ReservationTime time, Theme theme, Member member
    ) {
        validate(date, time, theme, member);
        this.id = id;
        this.date = date;
        this.reservationTime = time;
        this.theme = theme;
        this.member = member;
    }

    public static Reservation createWithoutId(
            LocalDate date, ReservationTime time,
            Theme theme, Member member
    ) {
        return new Reservation(null, date, time, theme, member);
    }

    public boolean isPastDateTime() {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        return reservationDateTime.isBefore(LocalDateTime.now());
    }

    public Long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation reservation = (Reservation) o;
        if (this.id == null || reservation.id == null) {
            return false;
        }
        return Objects.equals(id, reservation.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void validate(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("비어있는 예약날짜로 예약을 생성할 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new IllegalArgumentException("비어있는 예약시간으로는 예약을 생성할 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("비어있는 테마로는 예약을 생성할 수 없습니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("비어있는 멤버로는 예약을 생성할 수 없습니다.");
        }
    }
}
