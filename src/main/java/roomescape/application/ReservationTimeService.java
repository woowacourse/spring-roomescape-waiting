package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.request.ReservationTimeRequest;
import roomescape.application.dto.response.AvailableReservationTimeResponse;
import roomescape.application.dto.response.ReservationTimeResponse;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;
import roomescape.exception.BadRequestException;

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

    @Transactional
    public ReservationTimeResponse addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toReservationTime();

        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            String message = String.format("해당 시간의 예약 시간이 이미 존재합니다. (시작 시간: %s)", reservationTime.getStartAt());

            throw new BadRequestException(message);
        }

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    public List<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(LocalDate date, Long themeId) {
        List<AvailableReservationTimeDto> availableReservationTimeDtos = reservationTimeRepository
                .findAvailableReservationTimes(date, themeId);

        return availableReservationTimeDtos.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new DomainNotFoundException(String.format("해당 id의 예약 시간이 존재하지 않습니다. (id: %d)", id));
        }

        if (reservationRepository.existsByTimeId(id)) {
            throw new BadRequestException(String.format("해당 예약 시간을 사용하는 예약이 존재합니다. (예약 시간 id: %d)", id));
        }

        reservationTimeRepository.deleteById(id);
    }
}
