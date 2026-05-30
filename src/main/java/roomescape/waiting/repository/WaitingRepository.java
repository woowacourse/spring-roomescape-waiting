package roomescape.waiting.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {

    Optional<Waiting> save(Waiting waiting);

    boolean deleteById(long id);

    Optional<Waiting> findById(long id);

    Optional<Waiting> findEarliestBySlot(LocalDate date, long timeId, long themeId);

    int countEarlierWaitingsInSlot(LocalDate date, long timeId, long themeId, LocalDateTime createdAt);

    List<Waiting> findAllByCustomerNameAndReservationDateTimeAfter(String customerName, LocalDateTime now);
}
