package roomescape.waiting.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.dto.WaitingWithRank;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    boolean deleteById(long id);

    Optional<Waiting> findById(long id);

    Optional<Waiting> findEarliestBySlot(LocalDate date, long timeId, long themeId);

    List<WaitingWithRank> findAllWithRankByCustomerNameAndReservationDateTimeAfter(String customerName, LocalDateTime now);
}
