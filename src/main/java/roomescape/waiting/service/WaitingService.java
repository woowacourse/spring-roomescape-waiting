package roomescape.waiting.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationQueryService;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.controller.response.WaitingInfoResponse;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

@Service
@RequiredArgsConstructor
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationQueryService reservationQueryService;
    private final WaitingManager waitingManager;
    private final WaitingQueryService waitingQueryService;

    @Transactional
    public ReservationResponse waiting(ReserveCommand reserveCommand) {
        validateAvailableWaiting(reserveCommand);

        Waiting waiting = waitingManager.getWaiting(reserveCommand);
        Waiting saved = waitingRepository.save(waiting);

        return ReservationResponse.from(saved);
    }

    private void validateAvailableWaiting(ReserveCommand reserveCommand) {
        if (!reservationQueryService.existsReservation(reserveCommand.date(), reserveCommand.timeId())) {
            throw new InvalidArgumentException("예약 대기를 할 수 없습니다!");
        }
    }

    public List<WaitingInfoResponse> getAllInfo() {
        return waitingQueryService.getAllInfo();
    }

    public List<MyReservationResponse> getWaitingReservations(Long memberId) {
        return waitingQueryService.getWaitingReservations(memberId);
    }

    @Transactional
    public void deleteByUser(Long id, Long memberId) {
        if (!waitingRepository.existsByIdAndMemberId(id, memberId)) {
            throw new NotAuthorizationException("해당 예약 대기자가 아닙니다.");
        }

        delete(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!waitingRepository.existsById(id)) {
            throw new NotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }

        waitingRepository.deleteById(id);
    }
}
