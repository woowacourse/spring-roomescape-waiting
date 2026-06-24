package roomescape.domain;

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
import java.util.Objects;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_reservation",
        columnNames = {"date", "time_id", "theme_id"}
))
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Embedded
    private ReservationSlot slot;

    private ReservationStatus reservationStatus;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, ReservationSlot slot) {
        Objects.requireNonNull(member, "예약자는 필수값 입니다.");
        Objects.requireNonNull(slot, "예약 슬롯은 필수값 입니다.");
        this.id = id;
        this.member = member;
        this.slot = slot;
        this.reservationStatus = ReservationStatus.PENDING_PAYMENT;
    }

    public static Reservation createWithoutId(Member member, ReservationSlot slot) {
        return new Reservation(null, member, slot);
    }

    public void changeSlot(ReservationSlot slot) {
        this.slot = slot;
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

    public ReservationSlot getSlot() {
        return slot;
    }

    public LocalDate getDate() {
        return slot.getDate();
    }

    public ReservationTime getTime() {
        return slot.getTime();
    }

    public Theme getTheme() {
        return slot.getTheme();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public void updateStatus() {
        this.reservationStatus = ReservationStatus.CONFIRMED;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Reservation reservation = (Reservation) object;
        if (id != null && reservation.id != null) {
            return Objects.equals(id, reservation.id);
        }
        return Objects.equals(member, reservation.member)
                && Objects.equals(slot, reservation.slot)
                && Objects.equals(reservationStatus, reservation.reservationStatus);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(member, slot, reservationStatus);
    }
}
