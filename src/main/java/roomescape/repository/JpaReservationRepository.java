package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    boolean existsByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);
}
