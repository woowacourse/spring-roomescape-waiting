package roomescape.waiting.infrastructure;

import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.repository.WaitingRepository;

@Repository
public class JpaWaitingRepositoryAdapter implements WaitingRepository {

    private final JpaWaitingRepository jpaWaitingRepository;

    public JpaWaitingRepositoryAdapter(final JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(final Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }
}
