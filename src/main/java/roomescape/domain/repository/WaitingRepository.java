package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<Waiting> findBySlot(LocalDate date, Long timeId, Long themeId);

    Optional<Waiting> findById(Long id);

    void deleteById(Long id);

    void updateOrderIndex(Long id, int newOrderIndex);

    List<Waiting> findByName(String name);
}
