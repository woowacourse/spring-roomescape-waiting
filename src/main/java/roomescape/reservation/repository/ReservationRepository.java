package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Reservation;

public interface ReservationRepository {

    Long save(Reservation reservation);

    void deleteById(Long id);

    boolean existsByDateAndStartAtAndThemeId(LocalDate date, LocalTime startAt, Long themeId);

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

    List<Reservation> findByInFromTo(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                     final LocalDate dateTo);
}
