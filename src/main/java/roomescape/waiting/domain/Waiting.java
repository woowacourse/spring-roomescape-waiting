package roomescape.waiting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.bookingslot.domain.BookingSlot;

@Entity
@Table(name = "waitings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reservation_id", "member_id"})
)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waiting_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "waiting_status", nullable = false)
    private WaitingStatus waitingStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private BookingSlot bookingSlot;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Waiting(final WaitingStatus waitingStatus, final Member member, final BookingSlot bookingSlot) {
        this.waitingStatus = waitingStatus;
        this.member = member;
        this.bookingSlot = bookingSlot;
    }

    public Waiting() {
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final Waiting waiting)) {
            return false;
        }
        return Objects.equals(getId(), waiting.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    public Long getId() {
        return id;
    }

    public WaitingStatus getWaitingStatus() {
        return waitingStatus;
    }

    public BookingSlot getReservation() {
        return bookingSlot;
    }

    public Member getMember() {
        return member;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

