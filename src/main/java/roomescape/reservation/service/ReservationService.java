package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.InAlreadyReservationException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.exception.InAlreadyWaitingException;
import roomescape.waiting.service.WaitingQueryService;
import roomescape.waiting.service.WaitingService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationManager reservationManager;
    private final ReservedQueryService reservedQueryService;
    private final WaitingService waitingService;
    private final WaitingQueryService waitingQueryService;

    @Transactional
    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();

        isAlreadyReservedTime(date, timeId);

        Reservation reserved = reservationManager.reserve(reserveCommand);

        return ReservationResponse.from(reserved);
    }

    private void isAlreadyReservedTime(LocalDate date, Long timeId) {
        if (reservationRepository.existsByDateAndTimeId(date, timeId)) {
            throw new InvalidArgumentException("이미 예약이 존재하는 시간입니다.");
        }
    }

    @Transactional
    public ReservationResponse waiting(ReserveCommand reserveCommand) {
        validateAvailableWaiting(reserveCommand);

        Reservation waiting = reservationManager.waiting(reserveCommand);

        return ReservationResponse.from(waiting);
    }

    private void validateAvailableWaiting(ReserveCommand reserveCommand) {
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

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservedQueryService.getReserved(id);
        waitingService.promoteFirstWaitingToReservation(reservation.getDate(), reservation.getTimeId());
        reservationManager.delete(reservation);
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getAllReservations(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        responses.addAll(reservedQueryService.getReservations(memberId));
        responses.addAll(waitingQueryService.getWaitingReservations(memberId));

        return responses;
    }
}
