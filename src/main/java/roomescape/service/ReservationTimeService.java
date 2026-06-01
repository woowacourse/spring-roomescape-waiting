package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeSlot;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.TimeSlotResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.AlreadyInUseException;

@Service
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao) {
        this.reservationTimeDao = reservationTimeDao;
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeDao.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<TimeSlotResponse> findAvailableTime(Long id, String date) {
        List<TimeSlot> availableTimes = reservationTimeDao.findAvailableTime(id, date);

        return availableTimes.stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    @Transactional
    public ReservationTimeResponse save(ReservationTimeRequest request) {
        if (reservationTimeDao.existsByStartAt(request.startAt())) {
            throw new AlreadyExistsException("이미 존재하는 시간대이므로 추가할 수 없습니다.");
        }

        ReservationTime saved = new ReservationTime(request.startAt());

        ReservationTime time = reservationTimeDao.save(saved);

        return ReservationTimeResponse.from(time);
    }

    @Transactional
    public void delete(Long id) {
        if (reservationTimeDao.existsByTimeId(id)) {
            throw new AlreadyInUseException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeDao.delete(id);
    }
}
