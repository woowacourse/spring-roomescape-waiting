package roomescape.wating.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.wating.domain.Waiting;

public interface WaitingRepository {

    Long save(Waiting waiting);

    boolean deleteById(long id);

    Optional<Waiting> findById(long id);

    List<Waiting> findAllByCustomerNameAndReservationDateTimeAfter(
            String customerName,
            LocalDateTime now
    );
}
