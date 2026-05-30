package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private static final String CANNOT_DELETE_TIME_IN_USE = "ID %d번 시간을 사용 중인 예약이 존재하여 시간을 삭제할 수 없습니다.";
    private static final String NOT_FOUND_RESERVATION_TIME = "존재하지 않는 예약 시간입니다. (ID: %d)";

    private final ReservationTimeRepository timeRepository;

    public ReservationTimeService(ReservationTimeRepository timeRepository) {
        this.timeRepository = timeRepository;
    }

    public List<ReservationTime> getReservationTimes() {
        return timeRepository.findAll();
    }

    @Transactional
    public ReservationTime addTime(ReservationTime time) {
        return timeRepository.save(time);
    }

    @Transactional
    public void deleteTime(Long id) {
        timeRepository.deleteById(id);
    }

    public ReservationTime getById(Long id) {
        return timeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(NOT_FOUND_RESERVATION_TIME, id)));
    }
}
