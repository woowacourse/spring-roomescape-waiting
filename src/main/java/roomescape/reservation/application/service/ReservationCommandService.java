package roomescape.reservation.application.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.WaitingRepository;
import roomescape.reservationtime.application.dto.ReservationTimeResult;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.theme.application.dto.ThemeResult;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

@RequiredArgsConstructor
@Transactional
@Service
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository timeRepository;
    private final WaitingRepository waitingRepository;

    public ReservationResult save(ReservationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        ReservationSlot slot = request.toSlot(time.getStartAt());
        slot.validateReservable(request.now());

        Reservation reservation = request.toReservation(slot);

        if (reservationRepository.existsBySlot(reservation)) {
            Waiting waiting = request.toWaiting(slot);
            Waiting savedWaiting;
            try {
                savedWaiting = waitingRepository.save(waiting);
            } catch (DataIntegrityViolationException e) {
                throw new ConflictException("이미 해당 테마의 날짜와 시간에 대기를 신청했습니다.");
            }
            Long rank = waitingRepository.getRank(savedWaiting);
            return ReservationResult.waiting(
                    savedWaiting,
                    ThemeResult.from(theme),
                    ReservationTimeResult.from(time),
                    rank
            );
        }

        Reservation savedReservation;
        try {
            savedReservation = reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }

        return ReservationResult.confirmed(
                savedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time)
        );
    }

    public ReservationResult update(Long reservationId, ReservationUpdateCommand request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation updatedReservation = reservation.updateDateAndTime(request.date(), request.timeId(), time.getStartAt());
        updatedReservation.validateReservable(request.now());

        updateReservation(updatedReservation);

        Theme theme = themeRepository.findById(reservation.getSlot().themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        return ReservationResult.confirmed(
                updatedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time)
        );
    }

    public void deleteReservation(Long reservationId, LocalDateTime now) {
        ReservationSlot slot = reservationRepository.findSlotById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        slot.validateDeletable(now);

        if (reservationRepository.delete(reservationId) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
    }

    public void deleteWaiting(Long waitingId, LocalDateTime now) {
        ReservationSlot slot = waitingRepository.findSlotById(waitingId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 대기입니다."));

        slot.validateDeletable(now);

        if (waitingRepository.delete(waitingId) == 0) {
            throw new NotFoundException("존재하지 않는 대기입니다.");
        }
    }

    private void updateReservation(Reservation reservation) {
        checkAlreadyExistsDateAndTime(reservation);

        if (reservationRepository.update(reservation) == 0) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }
    }

    private void checkAlreadyExistsDateAndTime(Reservation reservation) {
        if (reservationRepository.existsDuplicateExcluding(reservation)) {
            throw new ConflictException("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }
}
