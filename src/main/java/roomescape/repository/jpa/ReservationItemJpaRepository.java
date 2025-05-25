package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationItem;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;

@Repository
public interface ReservationItemJpaRepository extends JpaRepository<ReservationItem, Long> {

    Optional<ReservationItem> findReservationItemByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);
}
