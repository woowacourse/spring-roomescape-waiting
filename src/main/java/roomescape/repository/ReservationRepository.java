package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationV2;

public interface ReservationRepository {

    Optional<Reservation> findById(final long id);

    List<Reservation> findByDate(LocalDate date);

    List<Reservation> findAll();

    List<ReservationV2> findAllReservationsV2();

    Reservation save(final Reservation reservation);

    ReservationV2 saveWithMember(final ReservationV2 reservation);

    int deleteById(final long id);

    boolean existsByDateAndTime(final LocalDate date, final ReservationTime time);

    List<ReservationV2> findByMemberIdAndThemeIdAndDateFromAndDateTo(final long memberId, final long themeId, final LocalDate dateFrom, final LocalDate dateTo);

    boolean existByDateAndTimeIdAndThemeId(final LocalDate date, final long timeId, final long themeId);

    List<ReservationV2> findByMemberId(Long memberId);
}
