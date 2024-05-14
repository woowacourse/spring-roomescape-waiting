package roomescape.reservation.repository;

import roomescape.reservation.domain.Reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(Long id);

    List<Reservation> findByDateBetween(LocalDate start, LocalDate end);

    List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId);

    List<Reservation> findByDateBetweenAndMemberIdAndThemeId(
            LocalDate start,
            LocalDate end,
            Long memberId,
            Long themeId
    );

    Optional<Reservation> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    int deleteById(Long id);

    Boolean existsByTimeId(Long timeId);

    Boolean existsByThemeId(Long themeId);
}
