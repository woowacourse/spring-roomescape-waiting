package roomescape.repository;

import roomescape.domain.Reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Reservation save(final Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByDateTimeAndThemeId(final LocalDate date, final LocalTime time, final long themeId);

    List<Reservation> findByDateAndTheme(final LocalDate date, final long themeId);

    boolean existsByThemeId(long themeId);

    boolean existsByTimeId(long timeId);

    List<Reservation> findReservationsByPeriodAndMemberAndTheme(long themeId, long memberId, LocalDate from, LocalDate to);

    int deleteById(final long id);
}
