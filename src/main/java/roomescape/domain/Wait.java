package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    private String name;

    @Embedded
    private Slot slot;

    public Wait() {
    }

    public Wait(Long id, LocalDateTime createdAt, String name, Slot slot) {
        validate(createdAt, name, slot);
        this.id = id;
        this.createdAt = createdAt;
        this.name = name;
        this.slot = slot;
    }

    public Wait(LocalDateTime createdAt, String name, Slot slot) {
        this(null, createdAt, name, slot);
    }

    public Wait withId(Long id) {
        return new Wait(id, createdAt, name, slot);
    }

    public boolean isSameUser(String name) {
        return this.name.equals(name);
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

    public String getName() {
        return name;
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
        return Objects.equals(createdAt, wait.createdAt) && Objects.equals(name, wait.name)
                && Objects.equals(slot, wait.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, name, slot);
    }

    private void validate(LocalDateTime createdAt, String name, Slot slot) {
        if (createdAt == null) {
            throw new InvalidDomainValueException("대기 신청 시간은 비어 있을 수 없습니다.");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidDomainValueException("대기자 이름은 비어 있을 수 없습니다.");
        }
        if (slot == null) {
            throw new InvalidDomainValueException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }
}
