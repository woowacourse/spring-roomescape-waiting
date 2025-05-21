package roomescape.waiting.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingManager {

    private final WaitingRepository waitingRepository;

    public Waiting findAndDelete(LocalDate date, Long timeId) {
        if (!waitingRepository.existsByDateAndTimeId(date, timeId)) {
            return null;
        }
        Waiting waiting = waitingRepository.findByDateAndTimeId(date, timeId).getFirst();
        waitingRepository.delete(waiting);
        return waiting;
    }
}
