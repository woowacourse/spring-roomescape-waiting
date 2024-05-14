package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import roomescape.domain.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByThemeId(Long themeId);

    //List<Reservation> findBy(Long themeId, Long memberId, LocalDate dateForm, LocalDate dateTo);
    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId); // TODO
}
