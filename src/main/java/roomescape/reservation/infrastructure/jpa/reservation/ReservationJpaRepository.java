package roomescape.reservation.infrastructure.jpa.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.reservation.domain.reservation.Reservation;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
