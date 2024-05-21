package roomescape.domain.reservation.repository.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.domain.reservation.domain.reservation.Reservation;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByThemeIdAndMemberIdAndDateValueBetween(Long themeId, Long memberId, LocalDate dateFrom,
                                                                  LocalDate dateTo);

    boolean existsByDateValueAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByMemberIdAndDateValueAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByDateValueAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
