package roomescape.adapter.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.ReservationTimeRepository;

@Repository
public class ReservationTimeRepositoryAdapter implements ReservationTimeRepository {

    private final ReservationTimeJpaRepository jpaRepository;

    public ReservationTimeRepositoryAdapter(ReservationTimeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ReservationTime> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<ReservationTime> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public ReservationTime save(ReservationTime time) {
        return jpaRepository.save(time);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<ReservationTime> findAvailable(LocalDate date, Long themeId) {
        return jpaRepository.findAvailable(date, themeId);
    }
}
