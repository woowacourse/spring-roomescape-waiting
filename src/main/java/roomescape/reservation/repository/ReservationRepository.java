package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateBetweenAndMemberIdAndThemeId(
            LocalDate start,
            LocalDate end,
            Long memberId,
            Long themeId
    );

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    List<Reservation> findByMemberId(Long id);

    Boolean existsByTimeId(Long timeId);

    Boolean existsByThemeId(Long themeId);

}
