package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationInfo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationInfoRepository extends JpaRepository<ReservationInfo, Long> {
    boolean existsByTimeId(Long timeId);

    Optional<ReservationInfo> findByDateValueAndTimeIdAndThemeId(LocalDate reservationDate, Long timeId, Long themeId);

    List<ReservationInfo> getReservationByThemeIdAndDateValue(Long themeId, LocalDate date);
}
