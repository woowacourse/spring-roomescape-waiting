package roomescape.wating.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.dto.WaitingWithRank;

public interface WaitingRepository {

    Long save(Waiting waiting);

    boolean deleteById(long id);

    Optional<Waiting> findById(long id);

    Optional<Waiting> findEarliestBySlotId(Long slotId);

    List<WaitingWithRank> findAllWithRankByCustomerNameAndCustomerEmailAndReservationDateTimeAfter(
            String customerName,
            String customerEmail,
            LocalDateTime now
    );
}
