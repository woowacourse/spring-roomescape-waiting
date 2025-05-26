package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.waiting.domain.Waiting;

public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {
    List<Waiting> findByMember_Id(Long id);

    long countByDateAndTheme_IdAndTime_IdAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalDateTime createdAt
    );

    long countByDateAndTheme_IdAndTime_Id(
            LocalDate date,
            Long themeId,
            Long timeId
    );

    boolean existsByMember_IdAndDateAndTime_Id(
            Long memberId,
            LocalDate date,
            Long timeId
    );

    List<Waiting> findByDateAndTheme_IdAndTime_IdOrderByCreatedAtAsc(
            LocalDate date,
            Long themeId,
            Long timeId
    );
}
