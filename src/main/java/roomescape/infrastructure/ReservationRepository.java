package roomescape.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDate;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByDateAndTimeIdAndThemeId(ReservationDate date, Long timeId, Long themeId);

    boolean existsByTimeId(Long id);

    List<Reservation> findAllByDateAndThemeId(ReservationDate date, Long themeId);

    boolean existsByThemeId(Long id);

    List<Reservation> findAllByMemberId(Long id);
}
