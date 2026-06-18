package roomescape.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.theme.Theme;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {
    Optional<Slot> findByDateAndTimeAndTheme(ReservationDate date, ReservationTime time, Theme theme);

    Optional<Slot> findByTheme(Theme theme);

    Optional<Slot> findByTime(ReservationTime time);
}
