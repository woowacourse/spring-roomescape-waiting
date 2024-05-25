package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservationtime.AvailableReservationTimeDto;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;

@Service
@Transactional(readOnly = true)
public class ReservationTimeService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public ReservationTimeService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
    }

    public List<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional
    public ReservationTimeResponse addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toReservationTime();
        validateDuplicatedStartAt(reservationTime);
        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedReservationTime);
    }

    private void validateDuplicatedStartAt(ReservationTime reservationTime) {
        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new IllegalArgumentException("해당 시간은 이미 존재합니다.");
        }
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        ReservationTime reservationTime = getReservationTimeById(id);
        validateReservedTime(reservationTime);
        reservationTimeRepository.delete(reservationTime);
    }

    private ReservationTime getReservationTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 id의 시간이 존재하지 않습니다."));
    }

    private void validateReservedTime(ReservationTime reservationTime) {
        if (reservationRepository.existsByTime(reservationTime)) {
            throw new IllegalArgumentException("해당 시간을 사용하는 예약이 존재합니다.");
        }
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(LocalDate date, Long themeId) {
        List<AvailableReservationTimeDto> availableReservationTimeDtos = reservationTimeRepository
                .findAvailableReservationTimes(date, themeId);

        return availableReservationTimeDtos.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }
}
