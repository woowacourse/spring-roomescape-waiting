package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Session;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateWaitingException;
import roomescape.exception.WaitingNotFoundException;
import roomescape.repository.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;

    public WaitingService(WaitingRepository waitingRepository) {
        this.waitingRepository = waitingRepository;
    }

    public Waiting findByIdOrThrow(long id) {
        return waitingRepository.findById(id)
                .orElseThrow(() -> new WaitingNotFoundException(id));
    }

    public List<Waiting> findByName(String name) {
        return waitingRepository.findByName(name);
    }

    public List<Waiting> findBySession(Session session) {
        return waitingRepository.findBySessionOrderByIdAsc(session);
    }

    public void validateNotDuplicate(Waiting waiting) {
        if (waitingRepository.existsByNameAndSession(waiting.getName(), waiting.getSession())) {
            throw new DuplicateWaitingException(waiting);
        }
    }

    @Transactional
    public Waiting save(Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    @Transactional
    public void deleteById(long id) {
        waitingRepository.deleteById(id);
    }
}
