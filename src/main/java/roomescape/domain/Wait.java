package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

@Entity
public class Wait {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private Slot slot;

    public Wait() {
    }

    public Wait(Long id, LocalDateTime createdAt, Member member, Slot slot) {
        validate(createdAt, member, slot);
        this.id = id;
        this.createdAt = createdAt;
        this.member = member;
        this.slot = slot;
    }

    public Wait(LocalDateTime createdAt, Member member, Slot slot) {
        this(null, createdAt, member, slot);
    }

    public Wait withId(Long id) {
        return new Wait(id, createdAt, member, slot);
    }

    public boolean isSameUser(Member other) {
        return this.member.getId().equals(other.getId());
    }

    public boolean isSameUser(String name) {
        return this.member.getName().equals(name);
    }

    public boolean isSameSlot(Slot otherSlot) {
        return slot.equals(otherSlot);
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
    }

    public boolean isFastCreatedAt(LocalDateTime otherCreatedAt) {
        return createdAt.isBefore(otherCreatedAt);
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
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

    public LocalDate getReservationDate() {
        return slot.getReservationDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Long getTimeId() {
        return slot.getTimeId();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public Long getThemeId() {
        return slot.getThemeId();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Wait wait = (Wait) object;
        return Objects.equals(createdAt, wait.createdAt) && Objects.equals(member, wait.member)
                && Objects.equals(slot, wait.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, member, slot);
    }

    private void validate(LocalDateTime createdAt, Member member, Slot slot) {
        if (createdAt == null) {
            throw new InvalidDomainValueException("대기 신청 시간은 비어 있을 수 없습니다.");
        }
        if (member == null) {
            throw new InvalidDomainValueException("대기자는 비어 있을 수 없습니다.");
        }
        if (slot == null) {
            throw new InvalidDomainValueException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }
}
