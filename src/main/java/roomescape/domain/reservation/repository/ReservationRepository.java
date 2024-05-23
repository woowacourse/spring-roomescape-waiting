package roomescape.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
