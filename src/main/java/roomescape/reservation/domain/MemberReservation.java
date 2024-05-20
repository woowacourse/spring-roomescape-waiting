package roomescape.reservation.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import roomescape.member.domain.Member;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class MemberReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @JoinColumn(name = "reservation_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;
    @Enumerated(value = EnumType.STRING)
    @ColumnDefault(value = "'CONFIRMATION'")
    private ReservationStatus status;
    @CreatedDate
    private LocalDateTime createdAt;

    protected MemberReservation() {
    }

    public MemberReservation(Long id, Member member, Reservation reservation, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.reservation = reservation;
        this.status = status;
    }

    public MemberReservation(Long id, Member member, Reservation reservation) {
        this(id, member, reservation, ReservationStatus.CONFIRMATION);
    }

    public MemberReservation(Member member, Reservation reservation, ReservationStatus status) {
        this(null, member, reservation, status);
    }

    public MemberReservation(Member member, Reservation reservation) {
        this(null, member, reservation, ReservationStatus.CONFIRMATION);
    }

    public boolean isSameMember(Member member) {
        return this.member.equals(member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
