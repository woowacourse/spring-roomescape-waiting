package roomescape.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllReservations();

    Reservation save(final Reservation reservation);

    void deleteById(final long id);

    List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo);

    boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
