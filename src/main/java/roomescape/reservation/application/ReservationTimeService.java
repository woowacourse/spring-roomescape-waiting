package roomescape.reservation.application;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceInUseException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.ReservationTimeCommandRepository;
import roomescape.reservation.domain.ReservationTimeQueryRepository;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeCommandRepository reservationTimeCommandRepository;
    private final ReservationTimeQueryRepository reservationTimeQueryRepository;

    public ReservationTimeResponse create(final CreateReservationTimeRequest request) {
        final LocalTime startAt = request.startAt();
        final List<ReservationTime> founds = reservationTimeQueryRepository.findAllByStartAt(startAt);
        if (!founds.isEmpty()) {
            throw new AlreadyExistException("해당 예약 시간이 이미 존재합니다. startAt = " + startAt);
        }

        final Long id = reservationTimeCommandRepository.save(new ReservationTime(startAt));
        final ReservationTime found = reservationTimeQueryRepository.getById(id);

        return ReservationTimeResponse.from(found);
    }

    public void deleteById(final Long id) {
        reservationTimeQueryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 예약 시간이 존재하지 않습니다. id = " + id));

        try {
            reservationTimeCommandRepository.deleteById(id);
        } catch (final DataIntegrityViolationException e) {
            throw new ResourceInUseException("해당 예약 시간을 사용하고 있는 예약이 존재합니다. id = " + id);
        }
    }

    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> reservationTimes = reservationTimeQueryRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}
