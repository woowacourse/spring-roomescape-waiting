package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTime> findTimes() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public ReservationTime createTime(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(startAt);
        return reservationTimeRepository.save(reservationTime);
    }

    @Transactional
    public void deleteTime(long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BusinessConflictException(ErrorCode.RESERVATION_TIME_IN_USE);
        }
        reservationTimeRepository.deleteById(id);
    }
}
