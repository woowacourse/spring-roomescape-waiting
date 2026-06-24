package roomescape.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeCommandService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeCommandService(
            ReservationTimeRepository timeRepository
    ) {
        this.timeRepository = timeRepository;
    }

    public ReservationTime save(ReservationTime time) {
        return timeRepository.save(time);
    }

    public void delete(Long id) {
        timeRepository.deleteById(id);
    }
}
