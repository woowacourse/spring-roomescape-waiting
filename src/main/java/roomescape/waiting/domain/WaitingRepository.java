package roomescape.waiting.domain;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationSpec;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void deleteById(Long id);

    List<Waiting> findAll();

    Optional<Waiting> findById(Long id);

    List<Waiting> findBySpec(ReservationSpec spec);

    List<Waiting> findByMemberId(Long memberId);
}
