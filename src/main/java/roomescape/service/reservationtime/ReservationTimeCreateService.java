package roomescape.service.reservationtime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.service.dto.request.ReservationTimeSaveRequest;

@Service
public class ReservationTimeCreateService {

    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeCreateService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Transactional
    public ReservationTime createReservationTime(ReservationTimeSaveRequest request) {
        if (reservationTimeRepository.findByStartAt(request.startAt()).isPresent()) {
            throw new InvalidRequestException("이미 존재하는 예약 시간입니다.");
        }

        ReservationTime newReservationTime = request.toEntity(request);
        return reservationTimeRepository.save(newReservationTime);
    }
}
