package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.AvailableReservationTimeResponse;
import roomescape.dto.response.ReservationTimeResponse;
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

    public List<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional
    public ReservationTimeResponse addReservationTime(ReservationTimeRequest reservationTimeRequest) {
        ReservationTime reservationTime = reservationTimeRequest.toReservationTime();

        if (reservationTimeRepository.existsByStartAt(reservationTime.getStartAt())) {
            throw new BadRequestException("해당 시간은 이미 존재합니다.");
        }

        ReservationTime savedReservationTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedReservationTime);
    }

    @Transactional
    public void deleteReservationTimeById(Long id) {
        if (!reservationTimeRepository.existsById(id)) {
            throw new DomainNotFoundException("해당 id의 시간이 존재하지 않습니다.");
        }

        if (reservationRepository.existsByTimeId(id)) {
            throw new BadRequestException("해당 시간을 사용하는 예약이 존재합니다.");
        }

        reservationTimeRepository.deleteById(id);
    }

    public List<AvailableReservationTimeResponse> getAvailableReservationTimes(LocalDate date, Long themeId) {
        List<AvailableReservationTimeDto> availableReservationTimeDtos = reservationTimeRepository
                .findAvailableReservationTimes(date, themeId);

        return availableReservationTimeDtos.stream()
                .map(AvailableReservationTimeResponse::from)
                .toList();
    }
}
