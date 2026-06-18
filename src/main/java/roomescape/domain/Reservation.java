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
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private Slot slot;

    public Reservation() {
    }

    public Reservation(Long id, Member member, Slot slot) {
        validate(member, slot);
        this.id = id;
        this.member = member;
        this.slot = slot;
    }

    public Reservation(Member member, Slot slot) {
        this(null, member, slot);
    }

    public Reservation withId(Long id) {
        return new Reservation(id, member, slot);
    }

    public boolean isSameUser(Member other) {
        return this.member.getId().equals(other.getId());
    }

    public boolean isPast(LocalDateTime now) {
        return slot.isPast(now);
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
        return slot.getReservationDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Reservation that = (Reservation) object;
        return Objects.equals(member, that.member) && Objects.equals(slot, that.slot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(member, slot);
    }

    private void validate(Member member, Slot slot) {
        if (member == null) {
            throw new InvalidDomainValueException("예약자는 비어 있을 수 없습니다.");
        }
        if (slot == null) {
            throw new InvalidDomainValueException("예약 슬롯은 비어 있을 수 없습니다.");
        }
    }
}
