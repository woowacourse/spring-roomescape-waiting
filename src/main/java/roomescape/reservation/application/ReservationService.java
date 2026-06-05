package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.ReservationErrorCode;
import roomescape.global.exception.customException.BusinessException;
import roomescape.global.exception.customException.EntityNotFoundException;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.dto.UserReservationResult;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationValidator reservationValidator;
    private final WaitingService waitingService;

    @Transactional
    public Reservation saveReservation(ReservationCreateCommand createCommand) {
        ReservationTime time = reservationTimeRepository.findById(createCommand.timeId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_TIME_INVALID));
        Theme theme = themeRepository.findById(createCommand.themeId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_THEME_INVALID));

        reservationValidator.validateAlreadyReservation(createCommand);
        Reservation reservation = Reservation.create(
                createCommand.name(),
                createCommand.date(),
                time,
                theme
        );
        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getReservationsByDateAndTheme(LocalDate date, Long themeId) {
        return reservationRepository.findByDateAndThemeId(date, themeId);
    }

    public UserReservationResult getReservationsByName(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<Waiting> waitings = waitingRepository.findByName(name);
        return UserReservationResult.from(reservations, waitings);
    }

    @Transactional
    public void updateReservationSchedule(ReservationUpdateCommand updateCommand) {
        ReservationTime time = reservationTimeRepository.findById(updateCommand.timeId())
                .orElseThrow(() -> new BusinessException(ReservationErrorCode.RESERVATION_TIME_INVALID));
        Reservation targetReservation = reservationRepository.findById(updateCommand.id())
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, updateCommand.id()));

        reservationValidator.validateAlreadyReservationExcludingSelf(updateCommand, targetReservation);
        Reservation updateReservation = targetReservation.update(
                updateCommand.name(),
                updateCommand.date(),
                time
        );
        try {
            reservationRepository.updateSchedule(updateReservation);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation targetReservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ReservationErrorCode.RESERVATION_NOT_FOUND,
                        id
                ));
        boolean deleted = reservationRepository.deleteById(id);
        if (!deleted) {
            throw new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id);
        }

        try {
            waitingService.promoteNextWaiting(
                    targetReservation.getDate(),
                    targetReservation.getTime(),
                    targetReservation.getTheme()
            );
        } catch (BusinessException | DataAccessException ignored) {
            // 대기자 승격 실패는 예약 취소를 막지 않음.
        }
    }

    @Transactional
    public void cancelReservation(Long id, String name) {
        Reservation targetReservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        ReservationErrorCode.RESERVATION_NOT_FOUND,
                        id
                ));
        targetReservation.cancel(name);
        boolean deleted = reservationRepository.deleteByIdAndName(id, name);
        if (!deleted) {
            throw new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id);
        }

        try {
            waitingService.promoteNextWaiting(
                    targetReservation.getDate(),
                    targetReservation.getTime(),
                    targetReservation.getTheme()
            );
        } catch (BusinessException | DataAccessException ignored) {
            // 대기자 승격 실패는 예약 취소를 막지 않음.
        }
    }
}
