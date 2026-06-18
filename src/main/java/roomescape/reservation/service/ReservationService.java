package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.ConflictException;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationValidator;
import roomescape.reservation.repository.JpaReservationRepository;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.theme.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationService {
    private final JpaReservationRepository jpaReservationRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            final JpaReservationRepository jpaReservationRepository,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final ReservationValidator reservationValidator
    ) {
        this.jpaReservationRepository = jpaReservationRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.reservationValidator = reservationValidator;
    }

    public List<Reservation> getAll() {
        return jpaReservationRepository.findAll();
    }

    public Reservation save(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        reservationValidator.validateCreateReferenceIds(themeId, timeId);

        Theme theme = themeService.getById(themeId);
        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation nonIdReservation = Reservation.createNew(name, date, theme, reservationTime);
        reservationValidator.validateReservable(nonIdReservation);

        if(jpaReservationRepository.existsByDateAndThemeIdAndTimeId(date, themeId, timeId)){
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return jpaReservationRepository.save(nonIdReservation);
    }

    public void saveWith(String name, LocalDate date, Theme theme, ReservationTime reservationTime) {
        Reservation nonIdReservation = Reservation.createNew(name, date, theme, reservationTime);

        jpaReservationRepository.save(nonIdReservation);
    }

    public Reservation findByDateAndThemeIdAndTimeId(final LocalDate date, final Long themeId, final Long timeId) {
        return jpaReservationRepository.findByDateAndThemeIdAndTimeId(date, themeId, timeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.RESERVATION_NOT_FOUND,
                        "해당 예약을 찾을 수 없습니다."
                ));
    }

    public void deleteById(final long id) {
        if (!jpaReservationRepository.existsById(id)){
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "삭제된 예약 데이터가 없습니다.");
        }

        jpaReservationRepository.deleteById(id);
    }

    @Transactional
    public Reservation deleteByIdAndName(final long id, final String name) {
        reservationValidator.validateLookupName(name);

        Reservation reservation = jpaReservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateCancelable(reservation);

        if (!jpaReservationRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND, "해당 예약 데이터가 존재하지 않습니다.");
        }

        jpaReservationRepository.deleteById(reservation.getId());

        return reservation;
    }

    public Reservation updateByIdAndName(
            final long id,
            final String name,
            final LocalDate date,
            final Long timeId
    ) {
        reservationValidator.validateLookupName(name);
        reservationValidator.validateUpdateReferenceIds(timeId);

        Reservation reservation = jpaReservationRepository.findByIdAndName(id, name)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.MY_RESERVATION_NOT_FOUND,
                        "조회한 이름으로 찾은 예약이 없습니다."
                ));

        reservationValidator.validateUpdatable(reservation);

        ReservationTime reservationTime = reservationTimeService.getById(timeId);
        Reservation updatedReservation = reservation.withDateAndTime(date, reservationTime);
        reservationValidator.validateReservable(updatedReservation);

        if (jpaReservationRepository.existsByDateAndThemeIdAndTimeIdAndIdNot(
                date,
                reservation.getTheme().getId(),
                timeId,
                reservation.getId()
        )) {
            throw new ConflictException(ErrorCode.RESERVATION_DUPLICATED, "동일한 시기에 예약을 할 수 없습니다.");
        }

        return jpaReservationRepository.save(updatedReservation);
    }
}
