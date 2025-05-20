package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    Optional<Reservation> findById(final long id);

    List<Reservation> findAll();

    Reservation save(final Reservation reservation);

    void deleteById(final long id);

    List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final long memberId, final long themeId, final LocalDate dateFrom, final LocalDate dateTo);

    boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId);

    List<Reservation> findByMemberId(final Long memberId);
}
