package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.WaitingDetail;
import roomescape.reservation.domain.repository.dto.WaitingOrderDetail;

public interface WaitingRepository {
    Optional<WaitingDetail> findDetailById(Long id);

    Optional<Waiting> findOldestByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId);

    Waiting save(Waiting waiting);

    Integer delete(Long id);

    List<WaitingOrderDetail> findByName(String name);
}
