package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void delete(Long id);

    Optional<Waiting> findById(Long id);

    List<WaitingWithOrder> findAll();

    List<WaitingWithOrder> findByName(String name);

    Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime,
            Theme theme);

    Optional<Waiting> findPromotableWaitingBySlot(ReservationSlot slot);

    boolean existsByNameAndSlot(String name, ReservationSlot slot);
}
