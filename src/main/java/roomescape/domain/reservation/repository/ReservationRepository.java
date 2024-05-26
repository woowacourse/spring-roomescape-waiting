package roomescape.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.domain.Status;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByDateAndTimeIdAndThemeIdAndStatus(LocalDate date, Long timeId, Long themeId, Status status);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberId(Long memberId);

    boolean existsByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByStatusIn(List<Status> statuses);
}
