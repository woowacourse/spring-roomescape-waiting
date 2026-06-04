package roomescape.reservation.application;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeRepository;
import roomescape.reservation.domain.ReservationSlotRepository;
import roomescape.reservation.presentation.request.CreateTimeRequest;
import roomescape.reservation.presentation.response.CreateTimeResponse;
import roomescape.reservation.presentation.response.ReservationTimeResponse;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.errors.ReservationTimeErrors;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public List<ReservationTimeResponse> getAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional
    public CreateTimeResponse createReservationTime(CreateTimeRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.save(request.toEntity());
        return CreateTimeResponse.from(reservationTime);
    }

    @Transactional
    public void deleteReservationTime(Long id) {
        if (reservationSlotRepository.countByTimeId(id) > 0) {
            throw new ConflictException(ReservationTimeErrors.RESERVATION_TIME_IN_USE);
        }
        reservationTimeRepository.deleteById(id);
    }
}
