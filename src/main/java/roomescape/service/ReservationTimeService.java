package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.ReservationTime;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationTimeNotFoundException;

@Service
public class ReservationTimeService {
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationDao reservationDao;

    public ReservationTimeService(ReservationTimeDao reservationTimeDao, ReservationDao reservationDao) {
        this.reservationTimeDao = reservationTimeDao;
        this.reservationDao = reservationDao;
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeDao.findAll();
    }

    @Transactional
    public ReservationTime save(LocalTime startAt) {
        if (reservationTimeDao.existsByStartAt(startAt)) {
            throw new ReservationConflictException("이미 존재하는 예약 시간입니다.");
        }
        return reservationTimeDao.save(startAt);
    }

    @Transactional
    public void delete(long id) {
        if (reservationDao.existsByTimeId(id)) {
            throw new ReservationConflictException("예약 또는 대기에 사용 중인 시간은 삭제할 수 없습니다.");
        }
        if (reservationTimeDao.delete(id) == 0) {
            throw new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다.");
        }
    }
}
