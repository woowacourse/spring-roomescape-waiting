package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    boolean existsBySameReservation(
            Long memberId,
            Long themeId,
            Long reservationTimeId,
            LocalDate date
    );

    void deleteById(Long id);

    Optional<Waiting> findById(Long id);

    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

    Optional<Waiting> findFirstOrderById(Long themeId, Long timeId, LocalDate date);
}
