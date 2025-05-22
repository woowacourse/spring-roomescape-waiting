package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;
import roomescape.persistence.dto.WaitingWithRankData;

@Repository
public class WaitingRepositoryImpl implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public WaitingRepositoryImpl(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }

    @Override
    public boolean hasAlreadyWaited(Long memberId, Long themeId, Long timeId, LocalDate date) {
        return jpaWaitingRepository.existsByMemberIdAndThemeIdAndTimeIdAndDate(memberId, themeId, timeId, date);
    }

    @Override
    public Optional<Waiting> findFirstWaiting(LocalDate date, Long themeId, Long timeId) {
        return jpaWaitingRepository.findFirstWaiting(date, themeId, timeId);
    }

    @Override
    public void delete(Waiting waiting) {
        jpaWaitingRepository.delete(waiting);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<WaitingWithRankData> findWaitingsWithRankByMemberId(Long memberId) {
        return jpaWaitingRepository.findWaitingsWithRankByMemberId(memberId);
    }

    @Override
    public List<Waiting> findAll() {
        return jpaWaitingRepository.findAllWaitings();
    }
}
