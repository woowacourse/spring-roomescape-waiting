package roomescape.model;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime registeredAt;

    @Embedded
    @AssociationOverrides({
            @AssociationOverride(name = "reservationTime", joinColumns = @JoinColumn(name = "reservation_time_id")),
            @AssociationOverride(name = "theme", joinColumns = @JoinColumn(name = "theme_id")),
            @AssociationOverride(name = "member", joinColumns = @JoinColumn(name = "member_id"))
    })
    private Reservation reservation;

    public Waiting(LocalDateTime registeredAt, Reservation reservation) {
        this.registeredAt = registeredAt;
        this.reservation = reservation;
    }

    public boolean ownBy(Member comparedMember) {
        return reservation.getMember().getId().equals(comparedMember.getId());
    }

    public String getThemeName() {
        return this.reservation.getTheme().getName();
    }

    public LocalDate getReservationDate() {
        return this.reservation.getDate();
    }

    public ReservationTime getReservationTime() {
        return this.reservation.getReservationTime();
    }

    public Theme getTheme() {
        return this.reservation.getTheme();
    }
}
