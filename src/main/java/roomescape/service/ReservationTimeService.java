package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.service.dto.command.ReservationTimeCreateCommand;
import roomescape.service.dto.result.ReservationTimeResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationTimeService {

    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationRepository reservationRepository;
    private final WaitingListRepository waitingListRepository;

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

        final boolean hasAnyOngoingWaitingList = waitingListRepository.existsByTimeId(timeId);
        if (hasAnyOngoingWaitingList) {
            throw new BusinessException(ErrorCode.TIME_HAS_WAITING_LIST);
        }

        final boolean deleted = reservationTimeRepository.delete(timeId);

        if (!deleted) {
            throw new BusinessException(ErrorCode.TIME_NOT_FOUND);
        }
    }

    public List<ReservationTimeResult> getTimes() {
        return reservationTimeRepository.findAll()
                .stream()
                .map(ReservationTimeResult::from)
                .toList();
    }
}
