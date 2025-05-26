package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(final ReservationWaiting reservationWaiting);

    Optional<ReservationWaiting> findByThemeIdAndTimeIdAndDate(final long themeId, final long timeId, final LocalDate date);

    void deleteById(final long id);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(final long memberId, final long themeId, final long timeId, final LocalDate date);

    List<ReservationWaiting> findByMemberId(final long memberId);

    int findWaitingOrderById(final long id);

    boolean existsById(final long id);

    List<ReservationWaiting> findAll();

    Optional<ReservationWaiting> findFirstByThemeIdAndTimeIdAndDateOrderByCreatedAtAsc(final long themeId, final long timeId, final LocalDate date);
}
