package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

public interface WaitingRepository {

    Optional<Waiting> findById(Long id);

    boolean existsByNameAndDateAndThemeIdAndTimeId(String name, LocalDate date, Long themeId, Long timeId);

    List<WaitingDetail> findByName(String name);

    Waiting save(Waiting waiting);

    void delete(Long id);

    void deleteOldestBySlot(LocalDate date, Long themeId, Long timeId);
}
