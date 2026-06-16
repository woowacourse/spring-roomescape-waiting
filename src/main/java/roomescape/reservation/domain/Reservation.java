package roomescape.reservation.domain;

import jakarta.persistence.Entity;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.domain.Member;
import roomescape.slot.domain.Slot;

@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_reservation_slot", columnNames = "slot_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    private Reservation(Long id, Member member, Slot slot) {
        this.id = id;
        this.member = Objects.requireNonNull(member, "member는 null일 수 없습니다.");
        this.slot = Objects.requireNonNull(slot, "slot은 null일 수 없습니다.");
    }

    public static Reservation create(Member member, Slot slot) {
        return new Reservation(null, member, slot);
    }

    public static Reservation of(Long id, Member member, Slot slot) {
        return new Reservation(id, member, slot);
    }

    public boolean isOwnedBy(Long memberId) {
        return Objects.equals(getMemberId(), memberId);
    }

    public void validateOwnedBy(Long memberId) {
        if (!isOwnedBy(memberId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_NOT_OWNED_BY_MEMBER, id);
        }
    }

    public void validateNotPast(LocalDateTime now) {
        slot.validateNotPast(now);
    }

    public void validateCancelable(LocalDateTime now) {
        validateNotPast(now);
    }

    public Long getSlotId() {
        return slot.getId();
    }

    public Long getMemberId() {
        return member.getId();
    }

}
