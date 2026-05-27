package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.controller.dto.request.ReservationTimeRequest;
import roomescape.controller.dto.response.ReservationTimeResponse;

@Service
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public List<ReservationTimeResponse> findAll() {
        List<ReservationTime> times = reservationTimeDao.findAll();

        return times.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeResponse save(ReservationTimeRequest request) {
        ReservationTime time = new ReservationTime(
                request.startAt()
        );

        ReservationTime saved = reservationTimeDao.save(time);

        return ReservationTimeResponse.from(saved);
    }

    public void delete(Long id) {
        if (!reservationTimeDao.existsById(id)) {
            throw new NotFoundException("존재하지 않는 시간입니다.");
        }

        if (reservationDao.existsByTimeId(id)) {
            throw new ConflictException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        reservationTimeDao.delete(id);
    }
}
