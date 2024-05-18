package roomescape.reservation.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationTimeDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    private final ReservationService reservationService;
    private final ReservationTimeRepository reservationTimeRepository;


    public ReservationTimeService(
            final ReservationService reservationService,
            final ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationService = reservationService;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse save(final ReservationTimeSaveRequest reservationTimeSaveRequest) {
        ReservationTime reservationTime = reservationTimeSaveRequest.toEntity();
        return ReservationTimeResponse.from(reservationTimeRepository.save(reservationTime));
    }

    public List<ReservationTimeResponse> getAll() {
        return StreamSupport.stream(reservationTimeRepository.findAll().spliterator(), false)
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTimeDeleteResponse delete(final long id) {
        validateDoesNotExists(id);
        reservationService.validateAlreadyHasReservationByTimeId(id);
        return new ReservationTimeDeleteResponse(reservationTimeRepository.deleteById(id));
    }

    private void validateDoesNotExists(final long id) {
        if (reservationTimeRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("[ERROR] (id : " + id + ") 에 대한 예약 시간이 존재하지 않습니다.");
        }
    }
}
