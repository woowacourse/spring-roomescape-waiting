package roomescape.reservation.repository;

import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.repository.dto.WaitingWithRankDto;

public interface WaitingRepository {

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId, Long memberId);

    boolean existsByParams(ReservationDate date, Long timeId, Long themeId);

    Optional<Waiting> findById(Long id);

    List<Waiting> findAll();

    List<WaitingWithRankDto> findWithRankByMemberId(Long memberId);

    Waiting save(Waiting waiting);

    Optional<Waiting> findEarliestByParams(ReservationDate date, Long timeId, Long themeId);

    void deleteById(Long id);
}
