package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationValidator;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;

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

    public Reservation findByDateAndThemeIdAndTimeId(final LocalDate date, final Long themeId, final Long timeId) {
        return reservationRepository.findByDateAndThemeIdAndTimeId(date, themeId, timeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "예약 정보가 없으면 대기 생성이 불가능합니다."
                ));
    }

    public void deleteById(final long id) {
        int affectedRowCount = reservationRepository.deleteById(id);

        if(affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "삭제된 예약 데이터가 없습니다.");
        }
    }

    public void deleteByIdAndName(final long id, final String name) {
        reservationValidator.validateLookupName(name);

        Reservation reservation = reservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateCancelable(reservation);

        int affectedRowCount = reservationRepository.deleteById(reservation.getId());

        if(affectedRowCount <= 0) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "삭제된 예약 데이터가 없습니다.");
        }
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
