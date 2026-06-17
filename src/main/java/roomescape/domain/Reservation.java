package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Embedded
    private ReservationSlot slot;

    protected Reservation() {
    }

    public Reservation(Long id, String name, ReservationSlot slot) {
        Objects.requireNonNull(name, "예약자명은 필수값 입니다.");
        Objects.requireNonNull(slot, "예약 슬롯은 필수값 입니다.");
        this.id = id;
        this.name = name;
        this.slot = slot;
    }

    public static Reservation createWithoutId(String name, ReservationSlot slot) {
        return new Reservation(null, name, slot);
    }

    public void changeSlot(ReservationSlot slot) {
        this.slot = slot;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
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

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        Reservation reservation = (Reservation) object;
        if (id != null && reservation.id != null) {
            return Objects.equals(id, reservation.id);
        }
        return Objects.equals(name, reservation.name)
                && Objects.equals(slot, reservation.slot);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(name, slot);
    }
}
