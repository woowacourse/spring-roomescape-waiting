package roomescape.domain.reservation;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.domain.member.Member;
import roomescape.domain.schedule.ReservationTime;
import roomescape.domain.theme.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationDetail detail;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    public Reservation(Member member, ReservationDetail detail, ReservationStatus status) {
        this.member = member;
        this.detail = detail;
        this.status = status;
    }

    public boolean isReservationOf(Long memberId) {
        return memberId.equals(member.getId());
    }

    public boolean isPast() {
        return detail.getSchedule().isBeforeNow();
    }

    public boolean isReserved() {
        return status.isReserved();
    }

    public void reserved() {
        this.status = ReservationStatus.RESERVED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation other)) return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDate getDate() {
        return detail.getDate();
    }

    public ReservationTime getReservationTime() {
        return detail.getReservationTime();
    }

    public Theme getTheme() {
        return detail.getTheme();
    }

    public LocalTime getTime() {
        return detail.getTime();
    }

    public boolean isReserved() {
        return status.isReserved();
    }

    public ReservationDetail getDetail() {
        return detail;
    }

    public void reserved() {
        this.status = ReservationStatus.RESERVED;
    }
}
