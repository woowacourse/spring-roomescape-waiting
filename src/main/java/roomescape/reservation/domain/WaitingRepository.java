package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void deleteById(Long waitingId);

    boolean existsById(Long waitingId);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long aLong, Long aLong1, Long memberId);

    Optional<Waiting> findById(Long waitingId);

    List<WaitingWithRank> findAllWaitingWithRankByMemberId(Long memberId);

    List<WaitingWithRank> findAllWaitingWithRank();
}
