package roomescape.reservation.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservation.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.dto.response.CreateReservationTimeResponse;
import roomescape.reservation.dto.response.FindReservationTimeResponse;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.repository.ReservationTimeRepository;

@Service
@Transactional
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeServiceValidator reservationTimeServiceValidator;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository,
                                  ReservationTimeServiceValidator reservationTimeServiceValidator) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeServiceValidator = reservationTimeServiceValidator;
    }

    public CreateReservationTimeResponse createReservationTime(
            CreateReservationTimeRequest createReservationTimeRequest) {
        reservationTimeServiceValidator.checkAlreadyExistsTime(createReservationTimeRequest.startAt());

        ReservationTime reservationTime = reservationTimeRepository.save(
                createReservationTimeRequest.toReservationTime());
        return CreateReservationTimeResponse.from(reservationTime);
    }


    @Transactional(readOnly = true)
    public List<FindReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(FindReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FindReservationTimeResponse getReservationTime(Long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않아 시간을 조회할 수 없습니다."));
        return FindReservationTimeResponse.from(reservationTime);
    }

    public void deleteById(Long id) {
        reservationTimeServiceValidator.validateExistReservationTime(id);
        reservationTimeServiceValidator.validateReservationTimeUsage(id);

        reservationTimeRepository.deleteById(id);
    }
}
