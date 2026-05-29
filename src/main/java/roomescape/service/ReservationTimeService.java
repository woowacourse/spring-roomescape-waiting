package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.ReservationTimeCommand;
import roomescape.service.dto.result.ReservationTimeResult;

@Service
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    public List<ReservationTimeResult> findReservationTimes() {
        List<ReservationTime> times = reservationTimeDao.findAll();

        return times.stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTimeResult createReservationTime(ReservationTimeCommand command) {
        if (reservationTimeDao.existsByStartAt(command.startAt())) {
            throw new ConflictException("이미 존재하는 예약 시간입니다.");
        }

        ReservationTime time = new ReservationTime(
                command.startAt()
        );

        ReservationTime saved = reservationTimeDao.save(time);

        return ReservationTimeResult.from(saved);
    }

    public void deleteReservationTime(Long id) {
        if (!reservationTimeDao.existsById(id)) {
            throw new NotFoundException("존재하지 않는 시간입니다.");
        }

        if (reservationDao.existsByTimeId(id)) {
            throw new ConflictException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        reservationTimeDao.delete(id);
    }
}