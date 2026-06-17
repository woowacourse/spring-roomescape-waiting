package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.TempReservationTimeRepository;
import roomescape.domain.ReservationTime;
import roomescape.dto.projection.ReservationTimeStatusProjection;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeStatusResponse;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(ReservationTimeRepository reservationTimeRepository) {
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> findAll() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<ReservationTimeStatusResponse> findAvailableTime(Long id, String date) {
        List<ReservationTimeStatusProjection> availableTimes = reservationTimeRepository.findAvailableTime(id, date);

        return availableTimes.stream()
                .map(ReservationTimeStatusResponse::from)
                .toList();
    }

    @Transactional
    public ReservationTimeResponse save(ReservationTimeRequest request) {
        if (reservationTimeRepository.existsReservationTimeByStartAt(request.startAt())) {
            throw new IllegalArgumentException("이미 존재하는 시간대이므로 추가할 수 없습니다.");
        }

        ReservationTime saved = new ReservationTime(request.startAt());

        ReservationTime time = reservationTimeRepository.save(saved);

        return ReservationTimeResponse.from(time);
    }

    @Transactional
    public void delete(Long id) {
        if (reservationTimeRepository.existsReservationTimeById(id)) {
            throw new IllegalArgumentException("해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
        }
        reservationTimeRepository.deleteById(id);
    }
}
