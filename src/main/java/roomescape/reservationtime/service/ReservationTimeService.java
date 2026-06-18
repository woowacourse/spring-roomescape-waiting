package roomescape.reservationtime.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.exception.ReservationTimeNotFoundException;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.service.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.service.dto.response.ReservationTimeResponse;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;

    @Transactional(readOnly = true)
    public List<ReservationTimeResponse> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationTime getById(final long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(ReservationTimeNotFoundException::new);
    }

    @Transactional
    public ReservationTimeResponse create(ReservationTimeCreateRequest data) {
        final ReservationTime reservationTime = ReservationTime.create(data.startAt());

        final ReservationTime savedTime = reservationTimeRepository.save(reservationTime);
        return ReservationTimeResponse.from(savedTime);
    }

    @Transactional
    public void delete(final Long timeId) {
        final boolean deleted = deleteReservationTime(timeId);

        if (!deleted) {
            throw new ReservationTimeNotFoundException();
        }
    }

    private boolean deleteReservationTime(final Long timeId) {
        return reservationTimeRepository.delete(timeId);
    }
}
