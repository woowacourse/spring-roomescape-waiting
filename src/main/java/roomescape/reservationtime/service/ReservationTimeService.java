package roomescape.reservationtime.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeInUseException;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.service.dto.response.ReservationTimeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    public List<ReservationTimeResponse> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    public ReservationTime getById(final long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(ReservationTimeNotFoundException::new);
    }

    public ReservationTimeResponse create(ReservationTimeCreateRequest data) {
        final ReservationTime reservationTime = ReservationTime.create(
                data.startAt()
        );

        final ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResponse.from(savedTime);
    }

    public void delete(final Long timeId) {
        final boolean deleted = deleteReservationTime(timeId);

        if (!deleted) {
            throw new ReservationTimeNotFoundException();
        }
    }

    private boolean deleteReservationTime(final Long timeId) {
        try {
            return reservationTimeRepository.delete(timeId);
        } catch (DataIntegrityViolationException exception) {
            throw new ReservationTimeInUseException(exception);
        }
    }
}
