package roomescape.domain.reservationtime;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.reservationslot.ReservationSlotRepository;
import roomescape.domain.reservationtime.admin.dto.CreateTimeRequest;
import roomescape.domain.reservationtime.admin.dto.CreateTimeResponse;
import roomescape.domain.reservationtime.admin.dto.ReservationTimeResponse;
import roomescape.support.exception.ConflictException;
import roomescape.support.exception.errors.ReservationTimeErrors;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    public CreateTimeResponse createReservationTime(CreateTimeRequest request) {
        ReservationTime reservationTime = reservationTimeRepository.save(request.toEntity());
        return CreateTimeResponse.from(reservationTime);
    }

    public List<ReservationTimeResponse> getAllReservationTime() {
        return reservationTimeRepository.findAll().stream()
            .map(ReservationTimeResponse::from)
            .toList();
    }

    public void deleteReservationTime(Long id) {
        if (reservationSlotRepository.countByTimeId(id) > 0) {
            throw new ConflictException(ReservationTimeErrors.RESERVATION_TIME_IN_USE);
        }
        reservationTimeRepository.deleteById(id);
    }
}
