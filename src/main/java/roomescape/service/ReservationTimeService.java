package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    @Transactional
    public ReservationTime create(LocalTime startAt) {
        Long id = reservationTimeRepository.insert(new ReservationTime(null, startAt));
        return reservationTimeRepository.findBy(id)
                .orElseThrow(() -> new IllegalArgumentException("생성된 예약 시간을 찾을 수 없습니다."));
    }

    @Transactional
    public void delete(Long id) {
        validateDeletable(id);
        reservationTimeRepository.delete(id);
    }

    private void validateDeletable(Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new BusinessException(ErrorCode.RESOURCE_IN_USE, "예약이 존재하는 시간은 삭제할 수 없습니다.");
        }
    }
}
