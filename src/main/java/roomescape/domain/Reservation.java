package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    private ReservationDate date;

    @ManyToOne(optional = false)
    private ReservationTime time;

    @ManyToOne(optional = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(Member member, ReservationDate date, ReservationTime time, Theme theme,
                       ReservationStatus status) {
        this(null, member, date, time, theme, status);
    }

    public Reservation(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme,
                       ReservationStatus status) {
        validateMember(member);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateStatus(status);
        this.member = member;
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("예약자는 비어있을 수 없습니다.");
        }
    }

    private void validateDate(ReservationDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 비어있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 비어있을 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("예약 테마는 비어있을 수 없습니다.");
        }
    }

    private void validateStatus(ReservationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("예약 상태는 비어있을 수 없습니다.");
        }
    }

    public boolean isPast() {
        return date.isBeforeNow() || date.isToday() && time.isBeforeNow();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDate getReservationDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
