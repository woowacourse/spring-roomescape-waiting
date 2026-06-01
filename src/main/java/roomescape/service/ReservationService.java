package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
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
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final Clock clock;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.clock = clock;
    }

    public List<Reservation> findReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<UserReservation> findUserReservations(String name, int page, int size) {
        return reservationRepository.findUserReservations(name, page, size).stream()
                .map(this::assignWaitingRank)
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

    private UserReservation assignWaitingRank(UserReservation entry) {
        if (entry.status() != ReservationStatus.WAITING) {
            return entry;
        }
        long rank = waitingRepository.findWaitingOrder(
                entry.id(), entry.theme(), entry.date(), entry.time());
        return UserReservation.from(entry.id(), entry.name(), entry.date(), entry.time(), entry.theme(), rank);
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
