package roomescape.persistence;

import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    public Optional<Waiting> findById(final Long id) {
        return jpaWaitingRepository.findById(id);
    }

    @Override
    public List<Waiting> findByThemeIdAndDateAndTimeId(final Long themeId, final LocalDate date, final Long reservationTimeId) {
        return jpaWaitingRepository.findByThemeIdAndDateAndTimeId(themeId, date, reservationTimeId);
    }

    @Override
    public int findMaxOrderByThemeIdAndDateAndTimeId(final Long themeId, final LocalDate date, final Long reservationTimeId) {
        return jpaWaitingRepository.findMaxOrderByThemeIdAndDateAndTimeId(themeId, date, reservationTimeId);
    }
}
