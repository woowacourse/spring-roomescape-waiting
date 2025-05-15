package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.reservation.domain.Reservation;

public interface ReservationListCrudRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    Optional<Reservation> findByTimeId(Long id);

    Optional<Reservation> findByThemeId(Long id);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);
}
