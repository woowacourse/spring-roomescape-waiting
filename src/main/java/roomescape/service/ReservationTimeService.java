package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private static final String NOT_FOUND_RESERVATION_TIME = "존재하지 않는 예약 시간입니다. (ID: %d)";

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<ReservationTime> getReservationTimes() {
        return timeRepository.findAll();
    }

    public ReservationTime findById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_RESERVATION_TIME, id)));
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
