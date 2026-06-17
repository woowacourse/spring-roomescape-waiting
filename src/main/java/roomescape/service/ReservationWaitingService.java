package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationRepository;
import roomescape.domain.MyReservation;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.service.exception.ReservationConflictException;

@Service
public class ReservationWaitingService {
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final Clock clock;

    public ReservationWaitingService(ReservationRepository reservationRepository,
                                     ReservationService reservationService,
                                     Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.clock = clock;
    }

    @Transactional
    public ReservationWaiting saveWaiting(String name, LocalDate date, long timeId, long themeId) {
        if (!reservationService.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)) {
            Reservation confirmed = reservationService.save(name, date, timeId, themeId);
            return new ReservationWaiting(confirmed, 0);
        }
        if (reservationService.existsByDateAndTimeIdAndThemeIdAndName(date, timeId, themeId, name)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        if (reservationRepository.existsByDateAndTime_IdAndTheme_IdAndNameAndStatus(date, timeId, themeId, name, ReservationStatus.WAITING)) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
        ReservationTime time = reservationService.getTime(timeId);
        Theme theme = reservationService.getTheme(themeId);
        Reservation waiting = new Reservation(name, date, LocalDateTime.now(clock), time, theme, ReservationStatus.WAITING);
        try {
            Reservation saved = reservationRepository.save(waiting);
            return toWaiting(saved)
                    .orElseThrow(() -> new ReservationConflictException("대기 신청에 실패했습니다."));
        } catch (ReservationConflictException e) {
            throw e;
        } catch (Exception e) {
            throw new ReservationConflictException("이미 대기 신청한 시간입니다.");
        }
    }

    @Transactional
    public void deleteWaiting(long id) {
        reservationRepository.findByIdAndStatusForUpdate(id, ReservationStatus.WAITING)
                .ifPresent(reservation -> {
                    reservation.validateCancellable(LocalDateTime.now(clock));
                    reservationRepository.deleteById(id);
                });
    }

    @Transactional(readOnly = true)
    public List<MyReservation> findAllWaiting() {
        return reservationRepository.findAllWaitingWithRank(ReservationStatus.WAITING);
    }

    @Transactional(readOnly = true)
    public List<ReservationWaiting> findAllWaitingByName(String username) {
        return reservationRepository.findByNameAndStatus(username, ReservationStatus.WAITING).stream()
                .map(this::toWaiting)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    private Optional<ReservationWaiting> toWaiting(Reservation reservation) {
        long order = reservationRepository.countWaitingBefore(
                reservation.getDate(), reservation.getTime().getId(), reservation.getTheme().getId(),
                ReservationStatus.WAITING, reservation.getCreatedAt(), reservation.getId()) + 1;
        return Optional.of(new ReservationWaiting(reservation, order));
    }
}
