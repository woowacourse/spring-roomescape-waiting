package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;

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

    public ReservationTimeResponse save(ReservationTimeRequest request) {
        if (reservationTimeDao.existsByStartAt(request.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 시간대이므로 추가할 수 없습니다.");
        }

        ReservationTime saved = new ReservationTime(request.startAt());

        ReservationTime time = reservationTimeDao.save(saved);

        return ReservationTimeResponse.from(time);
    }

    public void delete(Long id) {
        if (reservationTimeDao.existsByTimeId(id)) {
            throw new IllegalArgumentException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeDao.delete(id);
    }
}
