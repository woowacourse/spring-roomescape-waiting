package roomescape.reservation.waiting.repository;

import org.springframework.data.repository.CrudRepository;
import roomescape.reservation.waiting.domain.Waiting;

import java.util.List;

public interface WaitingRepository extends CrudRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long memberId);

}
