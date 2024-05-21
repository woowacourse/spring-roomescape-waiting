package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationDate;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationInfo, Long> {
    boolean existsByTimeId(Long timeId);

    boolean existsByThemeId(Long themeId);

    boolean existsByDateAndTimeId(ReservationDate reservationDate, Long timeId);

    List<ReservationInfo> getReservationByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, ReservationDate start, ReservationDate end);

    List<ReservationInfo> findAllByMemberId(Long memberId);

    List<ReservationInfo> getReservationByThemeIdAndDateValue(Long themeId, LocalDate date);
}
