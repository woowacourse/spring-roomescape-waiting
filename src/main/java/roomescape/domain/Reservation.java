package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.reservationStatus.ConfirmedStatus;
import roomescape.domain.reservationStatus.ReservationStatus;
import roomescape.domain.reservationStatus.PendingStatus;
import roomescape.domain.reservationStatus.ReservationStatusConverter;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(
        name = "reservation",
        uniqueConstraints = @UniqueConstraint(columnNames = "confirmed_theme_slot_id")
)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "theme_slot_id", nullable = false)
    private ThemeSlot themeSlot;

    @Column(name = "status", nullable = false)
    @Convert(converter = ReservationStatusConverter.class)
    private ReservationStatus reservationStatus;

    @Column(
            name = "confirmed_theme_slot_id",
            columnDefinition = "BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'CONFIRMED' THEN theme_slot_id ELSE NULL END)",
            insertable = false,
            updatable = false
    )
    private Long confirmedThemeSlotId;

    protected Reservation() {
    }

    public Reservation(String name, ThemeSlot themeSlot) {
        validate(name, themeSlot);
        this.id = null;
        this.name = name;
        this.themeSlot = themeSlot;
        this.reservationStatus = PendingStatus.getInstance();
    }

    public Reservation(Long id, String name, ThemeSlot themeSlot, ReservationStatus reservationStatus) {
        validate(name, themeSlot);
        this.id = id;
        this.name = name;
        this.themeSlot = themeSlot;
        this.reservationStatus = reservationStatus;
    }

    public static Reservation of(Long id, Reservation reservation) {
        return new Reservation(
                id,
                reservation.getName(),
                reservation.getThemeSlot(),
                reservation.getReservationStatus()
        );
    }

    private void validate(String name, ThemeSlot themeSlot) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("예약자 이름은 필수이며 비어있을 수 없습니다.");
        }
        if (themeSlot == null || themeSlot.getDate() == null) {
            throw new IllegalArgumentException("예약 날짜는 필수입니다.");
        }
        if (themeSlot.getTime() == null) {
            throw new IllegalArgumentException("유효하지 않은 예약 시간대입니다.");
        }
        if (themeSlot.getTheme() == null) {
            throw new IllegalArgumentException("유효하지 않은 테마입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isOwnedBy(String name) {
        return this.name.equals(name);
    }

    public LocalDate getDate() {
        return themeSlot.getDate();
    }

    public Time getTime() {
        return themeSlot.getTime();
    }

    public Theme getTheme() {
        return themeSlot.getTheme();
    }

    public ThemeSlot getThemeSlot() {
        return themeSlot;
    }

    public Long getThemeSlotId() {
        return themeSlot.getId();
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }

    public String getReservationStatusName() {
        return reservationStatus.getName();
    }

    public void changeStatus(ReservationStatus reservationStatus) {
        this.reservationStatus = reservationStatus;
    }

    public void changeThemeSlot(ThemeSlot themeSlot) {
        validate(name, themeSlot);
        this.themeSlot = themeSlot;
    }

    public void confirm() {
        reservationStatus.confirm(this);
    }

    public void cancel() {
        reservationStatus.cancel(this);
    }

    public void complete() {
        reservationStatus.complete(this);
    }

    public boolean isPendingStatus() {
        return reservationStatus == PendingStatus.getInstance();
    }

    public boolean isConfirmedStatus() {
        return reservationStatus == ConfirmedStatus.getInstance();
    }

    public boolean isModifiableStatus() {
        return isConfirmedStatus();
    }

    public boolean hasDifferentThemeSlot(Long themeSlotId) {
        return !themeSlot.hasSameId(themeSlotId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
