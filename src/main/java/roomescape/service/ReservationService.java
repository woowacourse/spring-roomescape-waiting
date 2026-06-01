package roomescape.service;

import java.util.ArrayList;
import java.util.Comparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.UserReservation;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            WaitingRepository waitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<Reservation> findReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<UserReservation> findUserReservations(String name, int page, int size) {
        List<UserReservation> userReservations = new ArrayList<>();

        for (Reservation reservation : reservationRepository.findByName(name)) {
            userReservations.add(
                    UserReservation.reserved(reservation.getId(), reservation.getName(), reservation.getDate(),
                            reservation.getTime(), reservation.getTheme()));
        }

        for (Waiting waiting : waitingRepository.findByName(name)) {
            userReservations.add(UserReservation.waiting(
                    waiting.getId(),
                    waiting.getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme(),
                    waitingRepository.countByThemeIdAndDateAndTimeIdAndIdLessThanEqual(
                            waiting.getId(),
                            waiting.getTheme(),
                            waiting.getDate(),
                            waiting.getTime()
                    )
            ));
        }

        userReservations.sort(Comparator
                .comparing(UserReservation::date)
                .thenComparing(ur -> ur.time().getStartAt())
                .thenComparing(ur -> ur.theme().getId()));

        int offset = page * size;

        return userReservations.stream()
                .skip(offset)
                .limit(size)
                .toList();
    }

    @Transactional
    public Reservation createReservation(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Reservation reservation = Reservation.create(name, date, time, theme, LocalDateTime.now(clock));
        checkDuplicated(reservation);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation updateReservation(long id, String name, LocalDate date, long timeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Reservation updated = reservation.changeSchedule(date, time, name, LocalDateTime.now(clock));
        checkDuplicated(updated);
        reservationRepository.update(updated);
        return updated;
    }

    @Transactional
    public void deleteUserReservation(long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.checkCancellable(name, LocalDateTime.now(clock));
        reservationRepository.delete(reservation);
    }

    private void checkDuplicated(Reservation reservation) {
        boolean duplicated = reservationRepository.findBySchedule(
                        reservation.getDate(),
                        reservation.getTime().getId(),
                        reservation.getTheme().getId()
                )
                .filter(found -> !reservation.isSameReservation(found))
                .isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
