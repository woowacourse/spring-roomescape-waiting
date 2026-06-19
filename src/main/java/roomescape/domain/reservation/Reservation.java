package roomescape.domain.reservation;

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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.DomainErrorCode;
import roomescape.domain.RoomEscapeException;
import roomescape.domain.member.Member;

import java.time.LocalDateTime;

@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uq_reservation",
                columnNames = {"slot_id", "member_id"}
        ))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private Slot slot;

    @Transient
    private Rank rank;

    @Column(name = "created_at", insertable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, Status status, Slot slot) {
        this.id = id;
        this.member = member;
        this.status = status;
        this.slot = slot;
    }

    public static Reservation create(Member member, Slot slot) {
        if (member == null) {
            throw new RoomEscapeException(DomainErrorCode.INVALID_INPUT, "회원은 null일 수 없습니다.");
        }
        return new Reservation(null, member, Status.WAITING, slot);
    }

    public Reservation withStatus(Status status) {
        return new Reservation(id, member, status, slot);
    }

    public Reservation withRank(Rank rank) {
        Reservation copy = new Reservation(id, member, status, slot);
        copy.rank = rank;
        return copy;
    }

    public void changeStatus(Status status) {
        this.status = status;
    }

    public void changeSlot(Slot slot) {
        this.slot = slot;
    }

    public boolean isApproved() {
        return status == Status.APPROVED;
    }

    public boolean isWaiting() {
        return status == Status.WAITING;
    }

    public boolean isSameMember(Reservation other) {
        return member.getId().equals(other.member.getId());
    }

    public void validateCancellable(LocalDateTime now) {
        slot.validateNotPast(now);
    }

    public void validateOwner(Long memberId) {
        if (!member.getId().equals(memberId)) {
            throw new RoomEscapeException(DomainErrorCode.FORBIDDEN, "본인의 예약만 취소할 수 있습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Status getStatus() {
        return status;
    }

    public Slot getSlot() {
        return slot;
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public Rank getRank() {
        return rank;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;
        if (id == null || that.id == null) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
