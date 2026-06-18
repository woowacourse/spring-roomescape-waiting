package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaReservationRepository reservationRepository;
    private final JpaReservationWaitingRepository reservationWaitingRepository;

    public ReservationTimeService(JpaReservationTimeRepository reservationTimeRepository,
                                  JpaReservationRepository reservationRepository,
                                  JpaReservationWaitingRepository reservationWaitingRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        ReservationTime reservationTime = new ReservationTime(null, startAt);
        return reservationTimeRepository.save(reservationTime);
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        reservationTimeRepository.deleteById(id);
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByTime_Id(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
        if (reservationWaitingRepository.existsByTime_Id(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약 대기가 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
