package roomescape.waiting.domain;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository {

    boolean existsByReservationId(long reservationId);

    boolean existsByReservationIdAndMemberId(long reservationId, long memberId);

    Waiting save(Waiting waiting);

    void deleteById(long id);

    List<Waiting> findAll();

    List<WaitingWithRank> findAllWithRankByMemberId(long memberId);

    Optional<Waiting> findTopByReservationId(long reservationId);
}
