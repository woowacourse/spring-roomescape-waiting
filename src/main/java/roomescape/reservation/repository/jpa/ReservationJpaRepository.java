package roomescape.reservation.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId);

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    List<Reservation> findAllByDateAndThemeId(LocalDate date, long themeId);

    List<Reservation> findAllByMemberId(Long id);

    List<Reservation> findAll(Specification<Reservation> spec);
}
