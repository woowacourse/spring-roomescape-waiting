package roomescape.waiting.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);
    Optional<Waiting> findById(Long id);
    Optional<Waiting> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId);
    void deleteByIdAndName(Long id, String name);
    List<Waiting> findByName(String name);
}
