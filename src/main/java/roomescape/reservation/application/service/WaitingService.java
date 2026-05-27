package roomescape.reservation.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.WaitingQueryResult;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

@RequiredArgsConstructor
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;

    @Transactional(readOnly = true)
    public List<WaitingQueryResult> findAllByName(String name) {
        return waitingRepository.findByName(name).stream()
                .map(WaitingQueryResult::from)
                .toList();
    }

    public int delete(Long id, String name) {
        WaitingDetail waitingDetail = getWaitingDetail(id);
        Waiting waiting = toWaiting(waitingDetail);

        validateOwner(name, waiting);

        return waitingRepository.delete(id);
    }


    private WaitingDetail getWaitingDetail(Long id) {
        return waitingRepository.findDetailById(id)
                .orElseThrow(() -> new RoomEscapeException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }

    private Waiting toWaiting(WaitingDetail waitingDetail) {
        return Waiting.builder()
                .id(waitingDetail.waitingId())
                .name(waitingDetail.username())
                .date(waitingDetail.date())
                .themeId(waitingDetail.themeId())
                .timeId(waitingDetail.timeId())
                .build();
    }

    private void validateOwner(String name, Waiting waiting) {
        if (!waiting.isOwner(name)) {
            throw new RoomEscapeException(ReservationErrorCode.FORBIDDEN_RESERVATION_ACCESS);
        }
    }
}
