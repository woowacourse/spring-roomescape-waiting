package roomescape.reservation.application.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import roomescape.global.RoomEscapeException;
import roomescape.reservation.application.dto.WaitingQueryResult;
import roomescape.reservation.application.event.ReservationScheduleVacatedEvent;
import roomescape.reservation.application.exception.ReservationErrorCode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservation.domain.repository.dto.WaitingDetail;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<WaitingQueryResult> findAllByName(String name) {
        return waitingRepository.findByName(name).stream()
                .map(WaitingQueryResult::from)
                .toList();
    }

    public Waiting save(Waiting waiting) {
        return waitingRepository.save(waiting);
    }

    public int delete(Long id, String name) {
        WaitingDetail waitingDetail = getWaitingDetail(id);
        Waiting waiting = toWaiting(waitingDetail);

        validateOwner(name, waiting);

        return waitingRepository.delete(id);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void promoteOldestWaiting(ReservationScheduleVacatedEvent event) {
        log.info("대기 승격 이벤트를 수신했습니다. date={}, themeId={}, timeId={}",
                event.date(), event.themeId(), event.timeId());

        waitingRepository.findOldestByDateAndThemeIdAndTimeId(
                        event.date(),
                        event.themeId(),
                        event.timeId()
                )
                .ifPresentOrElse(
                        this::promote,
                        () -> log.info("대기가 존재하지 않아 승격을 진행하지 않습니다. date={}, themeId={}, timeId={}",
                                event.date(), event.themeId(), event.timeId())
                );
    }

    private void promote(Waiting waiting) {
        log.info("대기를 예약으로 전환합니다. waitingId={}, date={}, themeId={}, timeId={}",
                waiting.getId(), waiting.getDate(), waiting.getThemeId(), waiting.getTimeId());

        Reservation reservation = Reservation.builder()
                .name(waiting.getName())
                .date(waiting.getDate())
                .themeId(waiting.getThemeId())
                .timeId(waiting.getTimeId())
                .build();

        reservationRepository.save(reservation);
        waitingRepository.delete(waiting.getId());
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
