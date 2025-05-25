package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;

public interface WaitingRepository {

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId);

    Optional<Waiting> findById(Long id);

    List<Waiting> findAll();

    List<Waiting> findAllByMemberId(Long memberId);
    
    List<Waiting> findAllOrderByAsc();

    Waiting save(Waiting waiting);
}
