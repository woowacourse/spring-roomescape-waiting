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

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationDetail reservationDetail;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @CreatedDate
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    public Reservation(Member member, ReservationDetail reservationDetail, ReservationStatus status) {
        this.member = member;
        this.reservationDetail = reservationDetail;
        this.status = status;
    }

    public boolean isReservationOf(Long memberId) {
        return memberId.equals(member.getId());
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
        return reservationDetail.getDate();
    }

    public ReservationTime getReservationTime() {
        return reservationDetail.getReservationTime();
    }

    public Theme getTheme() {
        return reservationDetail.getTheme();
    }

    public LocalTime getTime() {
        return reservationDetail.getTime();
    }
}
