package roomescape.reservation.application;

import java.time.LocalDate;
import java.util.List;
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
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository,
            ReservationValidator reservationValidator
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.reservationValidator = reservationValidator;
    }

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
        return reservationRepository.save(reservation);
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
        reservationRepository.updateSchedule(updateReservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation targetReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id));
        reservationRepository.deleteById(id);
        promoteFirstWaiting(targetReservation);
    }

    @Transactional
    public void cancelReservation(Long id, String name) {
        Reservation targetReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ReservationErrorCode.RESERVATION_NOT_FOUND, id));
        targetReservation.cancel(name);
        reservationRepository.deleteByIdAndName(id, name);
        promoteFirstWaiting(targetReservation);
    }

    private void promoteFirstWaiting(Reservation canceledReservation) {
        waitingRepository.findFirstBySlot(
                        canceledReservation.getDate(),
                        canceledReservation.getTime().getId(),
                        canceledReservation.getTheme().getId()
                )
                .filter(Waiting::isPromotable)
                .ifPresent(this::promoteWaiting);
    }

    private void promoteWaiting(Waiting waiting) {
        reservationRepository.save(Reservation.create(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime(),
                waiting.getTheme()
        ));
        waitingRepository.deleteById(waiting.getId());
    }
}
