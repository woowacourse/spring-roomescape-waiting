package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberIdAndThemeIdAndDateValueBetween(Long memberId,
                                                                  Long themeId,
                                                                  LocalDate dateFrom,
                                                                  LocalDate dateTo);

    List<Reservation> findByDateValueAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findAllByMemberId(Long memberId);

    boolean existsByDateValueAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByDateValueAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
