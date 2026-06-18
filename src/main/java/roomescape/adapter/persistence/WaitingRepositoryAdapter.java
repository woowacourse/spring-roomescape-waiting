package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Waiting;
import roomescape.domain.repository.WaitingRepository;

@Repository
public class WaitingRepositoryAdapter implements WaitingRepository {

    private final WaitingJpaRepository jpaRepository;

    public WaitingRepositoryAdapter(WaitingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaRepository.save(waiting);
    }

    @Override
    public List<Waiting> findBySlot(LocalDate date, Long timeId, Long themeId) {
        return jpaRepository.findByDateAndTime_IdAndTheme_IdOrderByOrderIndexAsc(date, timeId, themeId);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void updateOrderIndex(Long id, int newOrderIndex) {
        jpaRepository.updateOrderIndex(id, newOrderIndex);
    }

    @Override
    public List<Waiting> findByName(String name) {
        return jpaRepository.findByNameOrderByDateAscTime_StartAtAsc(name);
    }
}
