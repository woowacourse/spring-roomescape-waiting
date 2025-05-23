package roomescape.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.NotFoundException;
import roomescape.infrastructure.db.WaitingJpaRepository;
import roomescape.model.Waiting;

@Repository
@RequiredArgsConstructor
public class WaitingRepositoryImpl implements WaitingRepository {

    private final WaitingJpaRepository waitingJpaRepository;

    @Override
    public Waiting save(Waiting waiting) {
        return waitingJpaRepository.save(waiting);
    }

    @Override
    public Waiting findById(Long id) {
        return waitingJpaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 웨이팅입니다."));
    }

    @Override
    public void delete(Waiting waiting) {
        waitingJpaRepository.delete(waiting);
    }
}
