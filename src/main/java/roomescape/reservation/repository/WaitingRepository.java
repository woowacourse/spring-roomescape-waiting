package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.dto.response.WaitingWithRank;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public interface WaitingRepository {

    List<Waiting> findAll();

    void deleteById(Long id);

    Waiting save(Waiting upcomingReservationWithUnassignedId);

    int findMaxOrderByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId);


    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstByInfoDateAndInfoTimeAndInfoThemeOrderByTurnAsc(
            LocalDate date,
            ReservationTime time,
            Theme theme
    );

    boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
}
