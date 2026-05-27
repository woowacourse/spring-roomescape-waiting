package roomescape.service.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.repository.reservation.ReservationRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.reservationtime.ReservationTimeService;
import roomescape.domain.theme.Theme;
import roomescape.service.theme.ThemeService;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final ReservationValidator reservationValidator
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationValidator = reservationValidator;
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        reservationValidator.validateCreateReferenceIds(themeId, timeId);

        Theme theme = themeService.getById(themeId);
        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation nonIdReservation = Reservation.createNew(name, date, theme, reservationTime);
        reservationValidator.validateReservable(nonIdReservation);

        if(reservationRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)){
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.save(nonIdReservation);
    }

    public void deleteById(final long id) {
        reservationRepository.deleteById(id);
    }

    public void deleteByIdAndName(final long id, final String name) {
        reservationValidator.validateLookupName(name);

        Reservation reservation = reservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateCancelable(reservation);

        reservationRepository.deleteById(reservation.getId());
    }

    public Reservation updateByIdAndName(
            final long id,
            final String name,
            final LocalDate date,
            final Long timeId
    ) {
        reservationValidator.validateLookupName(name);
        reservationValidator.validateUpdateReferenceIds(timeId);

        Reservation reservation = reservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateUpdatable(reservation);

        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation updatedReservation = reservation.withDateAndTime(date, reservationTime);
        reservationValidator.validateReservable(updatedReservation);

        if (reservationRepository.existsByDateAndThemeIdAndTimeIdExcludingId(
                date,
                reservation.getTheme().getId(),
                timeId,
                reservation.getId()
        )) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return reservationRepository.update(updatedReservation);
    }
}
