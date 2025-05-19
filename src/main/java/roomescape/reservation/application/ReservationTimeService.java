package roomescape.reservation.application;

import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.resource.AlreadyExistException;
import roomescape.exception.resource.ResourceInUseException;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.infrastructure.ReservationTimeRepository;
import roomescape.reservation.ui.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.ui.dto.response.ReservationTimeResponse;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    @Transactional
    public ReservationTimeResponse create(final CreateReservationTimeRequest request) {
        final LocalTime startAt = request.startAt();
        final List<ReservationTime> founds = reservationTimeRepository.findAllByStartAt(startAt);
        if (!founds.isEmpty()) {
            throw new AlreadyExistException("해당 예약 시간이 이미 존재합니다. startAt = " + startAt);
        }

        final ReservationTime saved = reservationTimeRepository.save(new ReservationTime(startAt));

        return ReservationTimeResponse.from(saved);
    }

    @Transactional
    public void deleteById(final Long id) {
        reservationTimeRepository.getByIdOrThrow(id);

        try {
            reservationTimeRepository.deleteById(id);
        } catch (final DataIntegrityViolationException e) {
            throw new ResourceInUseException("해당 예약 시간을 사용하고 있는 예약이 존재합니다. id = " + id);
        }
    }

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> findAll() {
        final List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}
