package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationTimeDao;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.DeletionNotAllowedException;
import roomescape.exception.ResourceNotFoundException;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class ReservationTimeCommandService {

    private final ReservationTimeDao reservationTimeDao;

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        Long savedId = reservationTimeDao.create(ReservationTime.create(startAt));
        return reservationTimeDao.findById(savedId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 시간이 정상적으로 생성되지 않았습니다."));
    }

    @Transactional
    public void delete(Long reservationTimeId) {
        try {
            ReservationTime time = reservationTimeDao.findById(reservationTimeId)
                    .orElseThrow(() -> new ResourceNotFoundException("해당 시간이 존재하지 않습니다."));
            reservationTimeDao.delete(time);
        } catch (DataIntegrityViolationException e) {
            throw new DeletionNotAllowedException("예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
