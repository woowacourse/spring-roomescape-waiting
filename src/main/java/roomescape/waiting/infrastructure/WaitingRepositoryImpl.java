package roomescape.waiting.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

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
    public List<Waiting> findByMemberId(Long id) {
        return jpaWaitingRepository.findByMember_Id(id);
    }

    @Override
    public long countByDateAndThemeIdAndTimeIdAndCreatedAtBefore(
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalDateTime createdAt
    ) {
        return jpaWaitingRepository.countByDateAndTheme_IdAndTime_IdAndCreatedAtBefore(date, themeId, timeId, createdAt);
    }

    @Override
    public long countByDateAndThemeIdAndTimeId(LocalDate date, Long themeId, Long timeId) {
        return jpaWaitingRepository.countByDateAndTheme_IdAndTime_Id(date, themeId, timeId);
    }

    @Override
    public java.util.Optional<Waiting> findById(Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public boolean existsByMemberIdAndDateAndTimeId(
            Long memberId,
            LocalDate date,
            Long timeId
    ) {
        return jpaWaitingRepository.existsByMember_IdAndDateAndTime_Id(memberId, date, timeId);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAll();
    }

    @Override
    public List<Waiting> findByDateAndThemeIdAndTimeIdOrderByCreatedAtAsc(
            LocalDate date,
            Long themeId,
            Long timeId
    ) {
        return jpaWaitingRepository.findByDateAndTheme_IdAndTime_IdOrderByCreatedAtAsc(date, themeId, timeId);
    }

    @Override
    public void delete(Waiting waiting) {
        jpaWaitingRepository.delete(waiting);
    }
}
