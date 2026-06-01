package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
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
    public Reservation create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        reservationValidator.validateNotPast(date, time);
        return createReservation(name, date, timeId, themeId, time);
    }

    @Transactional
    public Reservation createByAdmin(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        return createReservation(name, date, timeId, themeId, time);
    }

    @Transactional
    public void delete(Long id, String name) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateUpdatableReservation(reservation, name);
        reservationRepository.delete(id);
    }

    @Transactional
    public void deleteByAdmin(Long id) {
        reservationRepository.delete(id);
    }

    @Transactional
    public Reservation update(Long id, String name, LocalDate date, Long timeId) {
        Reservation reservation = findReservation(id);
        reservationValidator.validateUpdatableReservation(reservation, name);

        Reservation updatedReservation = createUpdatedReservation(reservation, date, timeId);
        reservationValidator.validateUpdatePolicy(reservation, updatedReservation);

        try {
            reservationRepository.update(updatedReservation);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다.");
        }

        return findUpdatedReservation(id);
    }

    private Reservation save(Reservation reservation) {
        try {
            Long id = reservationRepository.insert(reservation);
            return reservationRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("생성된 예약을 찾을 수 없습니다."));
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약된 시간입니다.");
        }

    }

    private Reservation findReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약입니다."));
    }

    private Reservation findUpdatedReservation(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정된 예약을 찾을 수 없습니다."));
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findBy(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 시간입니다."));
    }

    private Reservation createReservation(String name, LocalDate date, Long timeId, Long themeId,
                                          ReservationTime time) {
        reservationValidator.validateNotReserved(date, timeId, themeId);
        Theme theme = findTheme(themeId);

        Reservation reservation = new Reservation(null, name, date, time, theme);
        return save(reservation);
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findBy(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    private Reservation createUpdatedReservation(Reservation reservation, LocalDate date, Long timeId) {
        reservationValidator.validateUpdateValueExists(date, timeId);

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
}
