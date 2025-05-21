package roomescape.reservationTime.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.common.exception.BusinessException;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;

@Service
public class ReservationTimeQueryService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeQueryService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTime> findAll() {
        return reservationTimeRepository.findAll();
    }

    public ReservationTime findById(final Long id) {
        return reservationTimeRepository.findById(id)
            .orElseThrow(() -> new BusinessException("해당 예약 시간을 찾을 수 없습니다."));
    }
}
