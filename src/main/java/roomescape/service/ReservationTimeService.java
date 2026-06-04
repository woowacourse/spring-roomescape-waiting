package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.common.exception.ConflictException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.service.dto.command.ReservationTimeCommand;
import roomescape.service.dto.result.ReservationTimeResult;

@Service
@Transactional(readOnly = true)
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

    @Transactional(rollbackFor = Exception.class)
    public ReservationTimeResult registerReservationTime(ReservationTimeCommand command) {
        ReservationTime time = new ReservationTime(
                command.startAt()
        );

        ReservationTime saved = reservationTimeDao.save(time);

        return ReservationTimeResult.from(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteReservationTime(Long id) {
        if (reservationDao.existsByTimeId(id)) {
            throw new ConflictException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }

        reservationTimeDao.delete(id);
    }
}
