package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface WaitingReservationRepository {

    List<Waiting> findFilteredReservations(Long themeId,
                                           Long memberId,
                                           LocalDate startDate,
                                           LocalDate endDate);

    List<Waiting> findAll();

    void deleteById(Long id);

    Waiting save(Waiting upcomingReservationWithUnassignedId);

    int findMaxOrderByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);

    List<Waiting> findByMemberId(Long id);

    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(
            LocalDate date,
            ReservationTime time,
            Theme theme
    );
}
