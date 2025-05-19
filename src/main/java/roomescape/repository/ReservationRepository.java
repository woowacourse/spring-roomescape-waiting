package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

public interface ReservationRepository {

    Optional<Reservation> findById(Long id);

    List<Reservation> findAllReservationsV2();

    Reservation saveWithMember(final Reservation reservation);

    int deleteById(final long id);

    boolean existsByDateAndTime(final LocalDate date, final ReservationTime time);

    List<Reservation> findByMemberIdAndThemeIdAndDateFromAndDateTo(final long memberId, final long themeId, final LocalDate dateFrom, final LocalDate dateTo);

    boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId);

    List<Reservation> findByMemberId(Long memberId);
}
