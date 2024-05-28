package roomescape.service.booking.time.module;

import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.time.ReservationTime;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.repository.ReservationTimeRepository;

@Service
@Transactional
public class TimeRegisterService {

    private final ReservationTimeRepository reservationTimeRepository;

    public TimeRegisterService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public Long registerTime(ReservationTimeRequest reservationTimeRequest) {
        validateTimeDuplicate(reservationTimeRequest.startAt());
        ReservationTime reservationTime = reservationTimeRequest.toEntity();
        return reservationTimeRepository.save(reservationTime).getId();
    }

    private void validateTimeDuplicate(LocalTime time) {
        if (reservationTimeRepository.existsByStartAt(time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 이미 등록된 시간은 등록할 수 없습니다.",
                    new Throwable("등록 시간 : " + time)
            );
        }
    }
}
