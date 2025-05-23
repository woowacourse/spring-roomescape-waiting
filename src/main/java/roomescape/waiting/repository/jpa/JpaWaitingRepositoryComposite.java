package roomescape.waiting.repository.jpa;

import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Repository
public class JpaWaitingRepositoryComposite implements WaitingRepository {
    private final JpaWaitingRepository jpaWaitingRepository;

    public JpaWaitingRepositoryComposite(JpaWaitingRepository jpaWaitingRepository) {
        this.jpaWaitingRepository = jpaWaitingRepository;
    }

    @Override
    public Waiting save(Waiting waiting) {
        return jpaWaitingRepository.save(waiting);
    }
}
