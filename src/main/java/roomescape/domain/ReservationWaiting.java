package roomescape.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation_waiting",
        columnNames = {"member_id", "reservation_date", "time_id", "theme_id"}
))
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime createdAt;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(name = "reservation_date"))
    private ReservationSlot slot;

    public ReservationWaiting() {
    }

    public ReservationWaiting(Long id, Member member, LocalDateTime createdAt, ReservationSlot slot) {
        Objects.requireNonNull(member, "예약 대기자는 필수값 입니다.");
        Objects.requireNonNull(createdAt, "예약 대기 생성일자는 필수값 입니다.");
        Objects.requireNonNull(slot, "예약 슬롯은 필수값 입니다.");
        this.id = id;
        this.member = member;
        this.createdAt = createdAt;
        this.slot = slot;
    }

    public static ReservationWaiting createWithoutId(Member member, LocalDateTime createdAt, ReservationSlot slot) {
        return new ReservationWaiting(null, member, createdAt, slot);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    public LocalDate getReservationDate() {
        return slot.getDate();
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

        ReservationWaiting reservationWaiting = (ReservationWaiting) object;
        if (id != null && reservationWaiting.id != null) {
            return Objects.equals(id, reservationWaiting.id);
        }
        return Objects.equals(member, reservationWaiting.member)
                && Objects.equals(createdAt, reservationWaiting.createdAt)
                && Objects.equals(slot, reservationWaiting.slot);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(member, createdAt, slot);
    }
}
