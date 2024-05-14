package roomescape.reservationtime.service;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.dto.request.CreateReservationTimeRequest;
import roomescape.reservationtime.dto.response.CreateReservationTimeResponse;
import roomescape.reservationtime.dto.response.FindReservationTimeResponse;
import roomescape.reservationtime.model.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository,
                                  final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public CreateReservationTimeResponse createReservationTime(
            final CreateReservationTimeRequest createReservationTimeRequest) {
        checkAlreadyExistsTime(createReservationTimeRequest.startAt());

        ReservationTime reservationTime = reservationTimeRepository.save(
                createReservationTimeRequest.toReservationTime());
        return CreateReservationTimeResponse.from(reservationTime);
    }

    private void checkAlreadyExistsTime(final LocalTime time) {
        if (reservationTimeRepository.existsByStartAt(time)) {
            throw new IllegalArgumentException("생성하려는 시간 " + time + "가 이미 존재합니다. 시간을 생성할 수 없습니다.");
        }
    }

    public List<FindReservationTimeResponse> getReservationTimes() {
        return reservationTimeRepository.findAll().stream()
                .map(FindReservationTimeResponse::from)
                .toList();
    }

    public FindReservationTimeResponse getReservationTime(final Long id) {
        ReservationTime reservationTime = reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("식별자 " + id + "에 해당하는 예약이 존재하지 않아 시간을 조회할 수 없습니다."));
        return FindReservationTimeResponse.from(reservationTime);
    }

    public void deleteById(final Long id) {
        validateExistReservationTime(id);
        validateReservationTimeUsage(id);

        reservationTimeRepository.deleteById(id);
    }

    private void validateExistReservationTime(final Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NoSuchElementException("식별자 " + id + "에 해당하는 시간이 존재하지 않습니다. 삭제가 불가능합니다.");
        }
    }

    private void validateReservationTimeUsage(final Long id) {
        if (reservationRepository.existsByTimeId(id)) {
            throw new IllegalStateException("식별자 " + id + "인 시간을 사용 중인 예약이 존재합니다. 삭제가 불가능합니다.");
        }
    }
}
