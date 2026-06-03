package roomescape.application.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeCommandService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeCommandService(
            ReservationTimeRepository timeRepository
    ) {
        this.timeRepository = timeRepository;
    }

    @Transactional
    public ReservationTime save(ReservationTime time) {
        return timeRepository.save(time);
    }

    @Transactional
    public void delete(Long id) {
        timeRepository.deleteById(id);
    }
}
