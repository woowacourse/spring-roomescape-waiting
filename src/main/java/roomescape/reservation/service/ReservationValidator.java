package roomescape.reservation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.service.WaitingQueryService;

@Component
@RequiredArgsConstructor
public class ReservationValidator {

    private final ReservedQueryService reservedQueryService;
    private final WaitingQueryService waitingQueryService;

    public void validateAvailableWaiting(ReserveCommand reserveCommand) {
        if (waitingQueryService.existWaiting(reserveCommand.memberId(), reserveCommand.date(),
                reserveCommand.timeId())) {
            throw new InAlreadyWaitingException("이미 예약 대기가 존재하는 시간입니다.");
        }

        if (reservedQueryService.existsReserved(reserveCommand.memberId(), reserveCommand.date(),
                reserveCommand.timeId())) {
            throw new InAlreadyReservationException("이미 예약한 사람입니다.");
        }

        if (reservedQueryService.existsReserved(reserveCommand.date(), reserveCommand.timeId())) {
            return;
        }

        throw new InvalidArgumentException("예약 대기를 할 수 없습니다!");
    }
}
