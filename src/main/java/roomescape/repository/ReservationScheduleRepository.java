package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.schdule.ReservationSchedule;

public interface ReservationScheduleRepository extends JpaRepository<ReservationSchedule, Long> {

    Optional<ReservationSchedule> findByReservationTime_IdAndTheme_IdAndReservationDate_Date(Long timeId, Long themeId,
                                                                                             LocalDate date);

    Optional<ReservationSchedule> findByReservationTime_Id(Long timeId);

    Optional<ReservationSchedule> findByTheme_Id(Long themeId);

    List<ReservationSchedule> findAllByTheme_IdAndReservationDate_Date(Long themeId, LocalDate date);
}
