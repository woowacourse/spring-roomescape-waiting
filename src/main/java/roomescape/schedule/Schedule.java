package roomescape.schedule;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private LocalDate date;

    @ManyToOne
    private ReservationTime reservationTime;

    @ManyToOne
    private Theme theme;

    public Schedule(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
    }

    public boolean isPast() {
        if (date.isBefore(LocalDate.now())) {
            return true;
        }
        if (date.equals(LocalDate.now()) && reservationTime.isBefore(LocalTime.now())) {
            return true;
        }
        return false;
    }
}
