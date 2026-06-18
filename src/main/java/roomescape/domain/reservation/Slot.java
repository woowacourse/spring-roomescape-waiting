package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.domain.theme.Theme;

@Entity
@NoArgsConstructor
@Getter
public class Slot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ReservationDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    private Slot(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }

    public static Slot load(Long id, ReservationDate date, ReservationTime time, Theme theme) {
        return new Slot(id, date, time, theme);
    }

    public static Slot create(ReservationDate date, ReservationTime time, Theme theme) {
        return new Slot(null, date, time, theme);
    }

    public boolean isSame(Slot target) {
        return id.equals(target.id);
    }

    public boolean isBefore(LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date.getValue(), time.getStartAt());

        return reservationDateTime.isBefore(now);
    }
}
