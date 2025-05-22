package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;
import roomescape.waiting.domain.WaitingStatus;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryImpl(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public List<Waiting> findByMemberIdAndStatus(Long id, WaitingStatus status) {
        return jpaWaitingRepository.findByMember_IdAndStatus(id, status);
    }

    @Override
    public long countByDateAndThemeIdAndTimeIdAndStatusAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            WaitingStatus status,
            LocalDateTime createdAt
    ) {
        return jpaWaitingRepository.countByDateAndTheme_IdAndTime_IdAndStatusAndCreatedAtBefore(date, themeId, timeId, status, createdAt);
    }

    @Override
    public long countByDateAndThemeIdAndTimeIdAndStatus(LocalDate date, Long themeId, Long timeId, WaitingStatus status) {
        return jpaWaitingRepository.countByDateAndTheme_IdAndTime_IdAndStatus(date, themeId, timeId, status);
    }
}
