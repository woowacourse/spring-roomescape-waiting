package roomescape.application.query;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeQueryService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeQueryService(
            ReservationTimeRepository timeRepository
    ) {
        this.timeRepository = timeRepository;
    }

    public ReservationTime getById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약시간입니다. Id: " + id));
    }

    public List<ReservationTime> findAll() {
        return timeRepository.findAllByOrderByStartAtAsc();
    }
}
