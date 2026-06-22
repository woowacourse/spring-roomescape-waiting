package roomescape.domain;

import static roomescape.domain.exception.DomainErrorCode.UNAUTHORIZED_RESERVATION;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.exception.RoomEscapeException;

@Entity
@Table(
    name = "waitlist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "slot_id"})
)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Waitlist(Long id, Member member, Slot slot, LocalDateTime createdAt) {
        this.id = id;
        this.member = member;
        this.createdAt = createdAt;
        this.slot = slot;
    }

    public Waitlist(Member member, Slot slot, LocalDateTime createdAt) {
        this(null, member, slot, createdAt);
    }

    protected Waitlist() {
    }

    public void verifyCancelableBy(String name) {
        verifyReservedBy(name, "본인의 대기 예약만 취소할 수 있습니다.");
    }

    private void verifyReservedBy(String other, String message) {
        if (!this.member.getName().equals(other)) {
            throw new RoomEscapeException(UNAUTHORIZED_RESERVATION, message);
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public Slot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }
}
