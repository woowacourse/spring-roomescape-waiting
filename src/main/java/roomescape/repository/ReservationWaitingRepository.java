package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.ReservationWaiting;

public interface ReservationWaitingRepository {

    ReservationWaiting save(final ReservationWaiting reservationWaiting);

    ReservationWaiting findByThemeIdAndTimeIdAndDate(final long themeId, final long timeId, final LocalDate date);

    void deleteById(final long id);

    boolean existsByMemberIdAndThemeIdAndTimeIdAndDate(final long memberId, final long themeId, final long timeId, final LocalDate date);

    List<ReservationWaiting> findByMemberId(final long memberId);

    int findWaitingOrderById(final long id);

    boolean existsById(final long id);

}
