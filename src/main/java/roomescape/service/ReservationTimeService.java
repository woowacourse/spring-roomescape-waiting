package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeCreateCommand;
import roomescape.dto.ReservationTimeResult;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;

    public List<ReservationTimeResult> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResult::from)
                .toList();
    }

    public ReservationTimeResult create(final ReservationTimeCreateCommand data) {
        final ReservationTime reservationTime = ReservationTime.create(
                data.startAt(),
                data.endAt()
        );

        final ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        return ReservationTimeResult.from(savedTime);
    }

    public void delete(final Long timeId) {
        final boolean hasAnyOngoingReservation = reservationRepository.existsByTimeId(timeId);
        if (hasAnyOngoingReservation) {
            throw new BusinessException(ErrorCode.TIME_HAS_RESERVATION);
        }

        final boolean deleted = reservationTimeRepository.delete(timeId);

        if (!deleted) {
            throw new BusinessException(ErrorCode.TIME_NOT_FOUND);
        }
    }
}
