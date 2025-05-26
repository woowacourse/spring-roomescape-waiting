package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findByMember_IdAndStatus(Long id, WaitingStatus status);

    long countByDateAndTheme_IdAndTime_IdAndStatusAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status,
            LocalDateTime createdAt
    );

    long countByDateAndTheme_IdAndTime_IdAndStatus(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status
    );

    boolean existsByMember_IdAndDateAndTime_IdAndStatus(
            Long memberId,
            LocalDate date,
            Long timeId,
            WaitingStatus status);

    List<Waiting> findByDateAndTheme_IdAndTime_IdAndStatusOrderByCreatedAtAsc(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status
    );
}
