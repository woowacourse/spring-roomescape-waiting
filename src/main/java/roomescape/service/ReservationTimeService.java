package roomescape.service;

import java.time.LocalTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.domain.ReservationTimeRepository;

@Service
public class ReservationTimeService {

    public static final int DELETE_FAILED_COUNT = 0;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            final ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public ReservationTimeResponse addReservationTime(final ReservationTimeRequest request) {
        ReservationTime reservationTime = new ReservationTime(request.startAt());
        validateUniqueReservationTime(reservationTime);
        ReservationTime saved = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(saved);
    }

    public void removeReservationTime(final long id) {
        reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하는 시간이 없습니다."));
        try {
            reservationTimeRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("이미 예약이 존재해 시간을 삭제할 수 없습니다.");
        }
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
}
