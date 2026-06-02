package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationValidator reservationValidator;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationValidator reservationValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationValidator = reservationValidator;
    }

    public List<Reservation> findByName(String name) {
        return reservationRepository.findByName(name);
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Transactional
    public Reservation createByUser(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);

        Reservation reservation = new Reservation(null, name, date, time, theme);
        reservationValidator.validateCreatableByUser(reservation, now);

        return insertReservation(reservation);
    }

    @Transactional
    public Reservation createByAdmin(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);

        Reservation reservation = new Reservation(null, name, date, time, theme);
        reservationValidator.validateCreatableByAdmin(reservation);

        return insertReservation(reservation);
    }

    @Transactional
    public void deleteByUser(Long id, String name, LocalDateTime now) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateModifiableByUser(reservation, name, now);
        reservationRepository.delete(id);
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        reservationRepository.delete(id);
    }

    @Transactional
    public Reservation updateByUser(Long id, String name, LocalDate date, Long timeId, LocalDateTime now) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateModifiableByUser(reservation, name, now);

        Reservation updatedReservation = createUpdatedReservation(reservation, date, timeId);
        reservationValidator.validateUpdatedReservation(reservation, updatedReservation, now);

        updateReservation(updatedReservation);
        return updatedReservation;
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    private Reservation insertReservation(Reservation reservation) {
        try {
            return reservationRepository.insert(reservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다.");
        }
    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

    private Reservation createUpdatedReservation(Reservation reservation, LocalDate date, Long timeId) {
        if (date == null && timeId == null) {
            throw new RoomescapeException(ErrorCode.INVALID_INPUT, "변경할 날짜 또는 시간이 필요합니다.");
        }
        return new Reservation(
                reservation.getId(),
                reservation.getName(),
                resolveUpdateDate(reservation, date),
                resolveUpdateTime(reservation, timeId),
                reservation.getTheme());
    }

    private LocalDate resolveUpdateDate(Reservation reservation, LocalDate date) {
        if (date != null) {
            return date;
        }
        return reservation.getDate();
    }

    private ReservationTime resolveUpdateTime(Reservation reservation, Long timeId) {
        if (timeId != null) {
            return findReservationTime(timeId);
        }
        return reservation.getTime();
    }

    private void updateReservation(Reservation updatedReservation) {
        try {
            reservationRepository.update(updatedReservation);
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약된 시간입니다.");
        }
    }
}
