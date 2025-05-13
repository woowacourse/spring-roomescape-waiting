package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    long save(final Reservation reservation);

    List<Reservation> findAll();

    Optional<Reservation> findById(final Long id);

    List<Reservation> findByDateTimeTheme(final LocalDate date, final LocalTime time, final long themeId);

    List<Reservation> findByDateAndTheme(final LocalDate date, final long themeId);

    List<Reservation> findByThemeMemberDateRange(final Long themeId, final Long memberId, final LocalDate from, final LocalDate to);

    int deleteById(final long id);
}
