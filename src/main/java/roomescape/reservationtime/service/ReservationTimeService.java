package roomescape.reservationtime.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.reservationtime.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.controller.dto.response.ReservationTimeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private static final String RESERVATION_TIME_NOT_FOUND_MESSAGE = "존재하지 않는 예약 시간입니다.";
    private static final String RESERVATION_TIME_IN_USE_MESSAGE = "해당 시간에 예약이 존재하여 삭제할 수 없습니다.";

    private final ReservationTimeRepository reservationTimeRepository;

    public List<ReservationTimeResponse> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResponse::from)
                .toList();
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
            throw new NotFoundException(RESERVATION_TIME_NOT_FOUND_MESSAGE);
        }
    }

    private boolean deleteReservationTime(final Long timeId) {
        try {
            return reservationTimeRepository.delete(timeId);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(RESERVATION_TIME_IN_USE_MESSAGE, exception);
        }
    }
}
