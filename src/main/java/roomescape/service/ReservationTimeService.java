package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.ReservationTimeRequest;
import roomescape.service.dto.ReservationTimeResponse;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    public static final int DELETE_FAILED_COUNT = 0;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationRepository reservationRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
    }

    public ReservationTimeResponse addReservationTime(final ReservationTimeRequest request) {
        ReservationTime reservationTime = new ReservationTime(request.startAt());
        validateUniqueReservationTime(reservationTime);
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(saved);
    }

    public void removeReservationTime(final long id) {
        validateExistTime(id);
        validateExistReservation(id);
        reservationTimeRepository.deleteById(id);
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream().map(ReservationTimeResponse::from).toList();
    }

    private void validateUniqueReservationTime(final ReservationTime reservationTime) {
        final LocalTime startAt = reservationTime.getStartAt();
        if (reservationTimeRepository.existsByStartAt(startAt)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약 시간 입니다.");
        }
    }

    private void validateExistReservation(final long id) {
        if (reservationRepository.existByTimeId(id)) {
            throw new IllegalArgumentException("[ERROR] 예약이 존재하는 시간 이므로 삭제할 수 없습니다.");
        }
    }

    private void validateExistTime(final long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new NoSuchElementException("[ERROR] 존재하지 않는 시간 입니다.");
        }
    }
}
