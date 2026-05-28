package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.repository.ReservationTimeDao;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationTimeCommandService {

    private final ReservationTimeDao reservationTimeDao;

    public ReservationTime create(LocalTime startAt) {
        return reservationTimeDao.save(new ReservationTime(null, startAt));
    }

    public void delete(long reservationTimeId) {
        try {
            reservationTimeDao.deleteById(reservationTimeId);
        } catch (DataIntegrityViolationException e) {
            throw new DeletionNotAllowedException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
