package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.repository.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    public static final int DELETE_FAILED_COUNT = 0;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(final ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse addReservationTime(final ReservationTimeRequest request) {
        ReservationTime reservationTime = new ReservationTime(request.startAt());
        validateUniqueReservationTime(reservationTime);
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(saved);
    }

    public void removeReservationTime(final long id) {
        int deleteCounts = reservationTimeRepository.deleteById(id);
        if (deleteCounts == DELETE_FAILED_COUNT) {
            throw new IllegalArgumentException(String.format("[ERROR] 예약시간 %d번은 존재하지 않습니다.", id));
        }
    }

    public List<ReservationTimeResponse> findReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        return reservationTimes.stream().map(ReservationTimeResponse::from).toList();
    }

    private void validateUniqueReservationTime(final ReservationTime reservationTime) {
        final LocalTime startAt = reservationTime.getStartAt();
        if (reservationTimeRepository.existsByStartAt(startAt.toString())) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 예약 시간 입니다.");
        }
    }
}
