package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<Waiting> findByMemberId(Long id);

    long countByDateAndThemeIdAndTimeIdAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalDateTime createdAt
    );

    long countByDateAndThemeIdAndTimeId(
            LocalDate date,
            Long themeId,
            Long timeId
    );

    Optional<Waiting> findById(Long id);

    boolean existsByMemberIdAndDateAndTimeId(Long id, LocalDate date, Long aLong);

    List<Waiting> findAll();

    List<Waiting> findByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(
            LocalDate date,
            Long themeId,
            Long timeId
    );

    void delete(Waiting waiting);
}
