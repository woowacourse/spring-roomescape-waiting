package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationRepository;
import roomescape.dao.ReservationTimeRepository;
import roomescape.dao.ThemeRepository;
import roomescape.domain.MyReservation;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.Page;
import roomescape.service.exception.ReservationConflictException;
import roomescape.service.exception.ReservationNotFoundException;
import roomescape.service.exception.ReservationTimeNotFoundException;
import roomescape.service.exception.ThemeNotFoundException;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public ReservationService(ReservationRepository reservationRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, Clock clock) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    @Transactional
    public Reservation save(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = validateReservationTime(timeId);
        Theme theme = validateTheme(themeId);
        if (reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(date, timeId, themeId, ReservationStatus.CONFIRMED)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        Reservation reservation = new Reservation(name, date, LocalDateTime.now(clock), time, theme);
        try {
            return reservationRepository.save(reservation);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Unique")) {
                throw new ReservationConflictException("이미 예약된 시간입니다.");
            }
            throw e;
        }
    }

    @Transactional
    public Reservation update(long id, LocalDate date, long timeId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        LocalDateTime now = LocalDateTime.now(clock);
        reservation.validateCancellable(now);
        ReservationTime time = validateReservationTime(timeId);
        Reservation updated = reservation.withUpdated(date, time, now);
        if (reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(date, timeId, reservation.getTheme().getId(), ReservationStatus.CONFIRMED)) {
            throw new ReservationConflictException("이미 예약된 시간입니다.");
        }
        reservationRepository.updateDateAndTime(updated.getId(), updated.getDate(), updated.getTime());
        approveFirstWaitingIfExists(reservation, now);
        return reservationRepository.findById(updated.getId()).orElseThrow();
    }

    @Transactional
    public void delete(long id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ReservationNotFoundException("존재하지 않는 예약입니다."));
        LocalDateTime now = LocalDateTime.now(clock);
        reservation.validateCancellable(now);
        reservationRepository.deleteById(id);
        approveFirstWaitingIfExists(reservation, now);
    }

    private void approveFirstWaitingIfExists(Reservation slot, LocalDateTime now) {
        if (LocalDateTime.of(slot.getDate(), slot.getTime().getStartAt()).isBefore(now)) {
            return;
        }
        reservationRepository.findFirstByDateAndTime_IdAndTheme_IdAndStatusOrderByCreatedAtAscIdAsc(
                        slot.getDate(), slot.getTime().getId(), slot.getTheme().getId(), ReservationStatus.WAITING)
                .ifPresent(waiting -> reservationRepository.updateStatus(waiting.getId(), ReservationStatus.CONFIRMED));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllByName(String username) {
        return reservationRepository.findByNameAndStatus(username, ReservationStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public List<MyReservation> findAllMine(String username) {
        List<MyReservation> confirmed = reservationRepository.findByNameAndStatus(username, ReservationStatus.CONFIRMED)
                .stream()
                .map(r -> new MyReservation(r, null))
                .toList();
        List<MyReservation> waiting = reservationRepository.findWaitingWithRankByName(username, ReservationStatus.WAITING);
        return Stream.concat(confirmed.stream(), waiting.stream()).toList();
    }

    @Transactional(readOnly = true)
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return reservationRepository.existsByDateAndTime_IdAndTheme_IdAndStatus(date, timeId, themeId, ReservationStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public boolean existsByDateAndTimeIdAndThemeIdAndName(LocalDate date, long timeId, long themeId, String name) {
        return reservationRepository.existsByDateAndTime_IdAndTheme_IdAndNameAndStatus(date, timeId, themeId, name, ReservationStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public ReservationTime getTime(long timeId) {
        return validateReservationTime(timeId);
    }

    @Transactional(readOnly = true)
    public Theme getTheme(long themeId) {
        return validateTheme(themeId);
    }

    @Transactional(readOnly = true)
    public Page<Reservation> findAllWithCount(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date", "id"));
        List<Reservation> reservations = reservationRepository.findAllByStatus(ReservationStatus.CONFIRMED, pageRequest);
        long totalCount = reservationRepository.countByStatus(ReservationStatus.CONFIRMED);
        return new Page<>(reservations, totalCount);
    }

    private ReservationTime validateReservationTime(long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ReservationTimeNotFoundException("존재하지 않는 예약 시간입니다."));
    }

    private Theme validateTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new ThemeNotFoundException("존재하지 않는 테마입니다."));
    }
}
