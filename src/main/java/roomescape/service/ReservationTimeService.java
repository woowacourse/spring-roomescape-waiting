package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<ReservationTime> getReservationTimes() {
        return timeRepository.findAll();
    }

    public ReservationTime findById(Long id) {
        return timeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약 시간입니다."));
    }

    @Transactional
    public ReservationTime addTime(ReservationTime time) {
        return timeRepository.save(time);
    }

    @Transactional
    public void deleteTime(Long id) {
        timeRepository.deleteById(id);
    }
}
