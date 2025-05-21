package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.reservation.controller.response.MyReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.WaitingManager;
import roomescape.waiting.service.WaitingQueryService;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingManager waitingManager;
    private final ReservationManager reservationManager;
    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    @Transactional
    public ReservationResponse reserve(ReserveCommand reserveCommand) {
        LocalDate date = reserveCommand.date();
        Long timeId = reserveCommand.timeId();

        isAlreadyReservedTime(date, timeId);

        Reservation reserved = reservationManager.getReservation(reserveCommand);
        Reservation saved = reservationRepository.save(reserved);

        return ReservationResponse.from(saved);
    }

    private void isAlreadyReservedTime(LocalDate date, Long timeId) {
        if (reservationRepository.existsByDateAndTimeId(date, timeId)) {
            throw new InvalidArgumentException("이미 예약이 존재하는 시간입니다.");
        }
    }

    @Transactional
    public void deleteByUser(Long id, Long userId) {
        Reservation reservation = reservationQueryService.getReservation(id);

        if (!reservation.isOwner(userId)) {
            throw new NotAuthorizationException("해당 예약자가 아닙니다.");
        }

        delete(id);
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = reservationQueryService.getReservation(id);
        reservationRepository.delete(reservation);
        waitingToReservation(reservation);
    }

    private void waitingToReservation(Reservation reservation) {
        LocalDate date = reservation.getDate();
        Long timeId = reservation.getTimeId();

        Waiting waiting = waitingManager.findAndDelete(date, timeId);
        if (waiting != null) {
            Reservation newReservation = Reservation.from(waiting);
            reservationRepository.save(newReservation);
        }
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> getAllReservations(Long memberId) {
        List<MyReservationResponse> responses = new ArrayList<>();

        responses.addAll(reservationQueryService.getReservations(memberId));
        responses.addAll(waitingQueryService.getWaitingReservations(memberId));

        return responses;
    }

}
