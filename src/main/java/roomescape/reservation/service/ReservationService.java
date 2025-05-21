package roomescape.reservation.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.web.exception.NotAuthorizationException;
import roomescape.global.exception.InvalidArgumentException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.command.ReserveCommand;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.service.WaitingManager;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingManager waitingManager;
    private final ReservationManager reservationManager;

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
        if (!reservationRepository.existsById(id)) {
            throw new NotFoundException("예약을 찾을 수 없습니다.");
        }

        if (!reservationRepository.existsByIdAndMemberId(id, userId)) {
            throw new NotAuthorizationException("해당 예약자가 아닙니다.");
        }

        delete(id);
    }

    @Transactional
    public void delete(Long id) {
        Reservation reservation = getReservation(id);
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

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
    }
}
