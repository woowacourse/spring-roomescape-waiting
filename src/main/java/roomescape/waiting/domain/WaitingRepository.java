package roomescape.waiting.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    List<Waiting> findByMemberIdAndStatus(Long id, WaitingStatus status);

    long countByDateAndThemeIdAndTimeIdAndStatusAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status,
            LocalDateTime createdAt
    );

    long countByDateAndThemeIdAndTimeIdAndStatus(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status
    );
}
