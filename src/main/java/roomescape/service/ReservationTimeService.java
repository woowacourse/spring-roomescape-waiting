package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ReservationTimeRepository;
import roomescape.domain.ReservationTime;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository, ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public ReservationTime save(LocalTime startAt) {
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new ReservationConflictException("이미 존재하는 예약 시간입니다.");
        }
        return reservationTimeRepository.save(new ReservationTime(null, startAt));
    }

    @Transactional
    public void delete(long id) {
        if (reservationRepository.existsByTime_Id(id)) {
            throw new ReservationConflictException("예약에 사용 중인 시간은 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
