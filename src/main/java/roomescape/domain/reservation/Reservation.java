package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.domain.TimeStamp;
import roomescape.domain.member.Member;

@Entity
public class Reservation extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Member member;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(optional = false)
    private ReservationTime time;
    @ManyToOne(optional = false)
    private Theme theme;
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.reservationStatus = ReservationStatus.COMPLETE;
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public void validateCreatedAtAfterReserveTime(LocalDateTime createdAt) {
        LocalDateTime reservedDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservedDateTime.isBefore(createdAt)) {
            throw new IllegalArgumentException("현재 시간보다 과거로 예약할 수 없습니다.");
        }
    }

    public boolean isOwnedBy(long memberId) {
        return member.hasId(memberId);
    }

    public void updateMember(Member member) {
        this.member = member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
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

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }
}
