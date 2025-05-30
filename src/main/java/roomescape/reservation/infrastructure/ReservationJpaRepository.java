package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import roomescape.reservation.domain.Reservation;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    @NonNull
    @EntityGraph(attributePaths = {"member", "time", "theme"})
    List<Reservation> findAll();

    @EntityGraph(attributePaths = {"member", "time"})
    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    @EntityGraph(attributePaths = {"theme", "time"})
    List<Reservation> findAllByMemberId(long id);

    @NonNull
    @EntityGraph(attributePaths = {"member", "time", "theme"})
    Optional<Reservation> findById(long id);

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);
}
