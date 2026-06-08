package roomescape.reservation.application.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ConflictException;
import roomescape.global.exception.NotFoundException;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
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

    public ReservationApplicationResult save(ReservationApplicationCreateCommand request) {
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        ReservationTime time = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        ReservationSlot slot = request.toSlot(time.getStartAt());
        slot.validateReservable(request.now());

        if (reservationRepository.existsBySlot(slot)) {
            throw new ConflictException("이미 해당 날짜와 시간에 예약이 존재합니다.");
        }

        Reservation savedReservation = saveReservation(request.toReservation(slot));
        return ReservationApplicationResult.confirmed(
                savedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(time)
        );
    }

    public ReservationApplicationResult update(Long reservationId, ReservationUpdateCommand request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        ReservationTime updateTime = timeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Reservation updatedReservation = updateReservationSlot(request, updateTime.getStartAt(), reservation);
        promoteFirstWaitingToReservation(reservation.getSlot());

        Theme theme = themeRepository.findById(updatedReservation.getSlot().themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        return ReservationApplicationResult.confirmed(
                updatedReservation,
                ThemeResult.from(theme),
                ReservationTimeResult.from(updateTime)
        );
    }

    public void cancel(Long reservationId, LocalDateTime now) {
        ReservationSlot slot = reservationRepository.findSlotById(reservationId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약입니다."));

        slot.validateDeletable(now);

        reservationRepository.delete(reservationId);
        promoteFirstWaitingToReservation(slot);
    }

    private Reservation updateReservationSlot(ReservationUpdateCommand request, LocalTime startAt,
                                              Reservation reservation) {
        Reservation updatedReservation = reservation.updateDateAndTime(
                request.date(),
                request.timeId(),
                startAt,
                request.now()
        );

        validateNoDuplicateReservationSlot(updatedReservation);
        if (isUpdateFailed(updatedReservation)) {
            throw new NotFoundException("존재하지 않는 예약입니다.");
        }

        return updatedReservation;
    }

    private boolean isUpdateFailed(Reservation updatedReservation) {
        return reservationRepository.update(updatedReservation.getId(), updatedReservation.getSlot()) == 0;
    }

    private void promoteFirstWaitingToReservation(ReservationSlot slot) {
        Optional<Waiting> firstWaitingBySlot = waitingRepository.findFirstBySlot(slot);
        firstWaitingBySlot.ifPresent(waiting -> {
            waitingRepository.delete(waiting.getId());

            saveReservation(Reservation.builder()
                    .memberName(waiting.getMemberName())
                    .slot(waiting.getSlot())
                    .build());
        });
    }

    private Reservation saveReservation(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }

    private void validateNoDuplicateReservationSlot(Reservation reservation) {
        if (reservationRepository.existsDuplicateExcluding(reservation)) {
            throw new ConflictException("변경하려는 날짜와 시간에 이미 예약이 존재합니다.");
        }
    }
}
