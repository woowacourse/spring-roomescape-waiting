package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.member.Member;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, Theme theme, LocalDate date, ReservationTime time,
                       LocalDateTime createdAt, BookStatus status) {
        validateCreatedAtAfterReserveTime(date, time.getStartAt(), createdAt);
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.createdAt = createdAt;
        this.status = status;
    }

    public Reservation(Member member, Theme theme, LocalDate date, ReservationTime time,
                       LocalDateTime createdAt, BookStatus status) {
        this(null, member, theme, date, time, createdAt, status);
    }

    private void validateCreatedAtAfterReserveTime(LocalDate date, LocalTime startAt, LocalDateTime createdAt) {
        LocalDateTime reservedDateTime = LocalDateTime.of(date, startAt);
        if (reservedDateTime.isBefore(createdAt)) {
            throw new IllegalArgumentException("현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    public boolean isNotModifiableBy(Member member) {
        return member.isNotAdmin() && isNotOwnedBy(member);
    }

    private boolean isNotOwnedBy(Member member) {
        return !member.equals(this.member);
    }

    public boolean isBooked() {
        return status == BookStatus.BOOKED;
    }

    public void book() {
        status = status.book();
    }

    public void cancelWaiting() {
        status = status.cancelWaiting();
    }

    public void cancelBooking() {
        status = status.cancelBooking();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation other)) {
            return false;
        }
        return Objects.equals(id, other.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
