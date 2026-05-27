package roomescape.service;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.*;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public ReservationWaitingService(
            ReservationRepository reservationRepository,
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public List<WaitingResult> findByName(String name) {
        return reservationWaitingRepository.findByName(name).stream()
                .map(waiting -> new WaitingResult(
                            waiting.getId(),
                            waiting.getName(),
                            waiting.getDate(),
                            waiting.getTime(),
                            waiting.getTheme(),
                            calculateTurn(waiting)))
                .toList();
    }

    @Transactional
    public WaitingResult create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);
        ReservationWaiting waiting = new ReservationWaiting(null, name, date, time, theme);

        validateWaiting(waiting);

        ReservationWaiting saved = save(waiting);
        return new WaitingResult(
                saved.getId(),
                saved.getName(),
                saved.getDate(),
                saved.getTime(),
                saved.getTheme(),
                calculateTurn(saved));
    }

    @Transactional
    public void delete(Long id, String name) {
        ReservationWaiting waiting = findWaiting(id);
        validateUpdatableReservation(waiting, name);
        reservationWaitingRepository.delete(id);
    }

    private long calculateTurn(ReservationWaiting waiting) {
        return reservationWaitingRepository.countEarlierWaitings(waiting.getId()) + 1;
    }

    @NonNull
    private Theme findTheme(Long themeId) {
        return themeRepository.findBy(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    @NonNull
    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findBy(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));
    }

    @NonNull
    private ReservationWaiting save(ReservationWaiting waiting) {
        Long id = reservationWaitingRepository.insert(waiting);
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("생성된 예약 대기를 찾을 수 없습니다."));
    }

    @NonNull
    private ReservationWaiting findWaiting(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));
    }

    private void validateWaiting(ReservationWaiting waiting) {
        validateNotPastDateAndTime(waiting);
        validateAlreadyReserved(waiting);
        validateNotOwnReservationSlot(waiting);
        validateNotDuplicateWaiting(waiting);
    }

    private void validateNotOwnReservationSlot(ReservationWaiting waiting) {
        if (reservationRepository.existsByNameWith(
                waiting.getName(),
                waiting.getDate(),
                waiting.getTime().getId(),
                waiting.getTheme().getId())) {
            throw new WaitingNotAllowedForOwnReservationException("본인이 예약한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateAlreadyReserved(ReservationWaiting waiting) {
        if (!reservationRepository.existsWith(
                waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new InvalidInputException("예약 가능한 시간에는 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotPastDateAndTime(ReservationWaiting waiting) {
        LocalDateTime reservationDateTime = LocalDateTime.of(waiting.getDate(), waiting.getTime().getStartAt());
        if (reservationDateTime.isBefore(LocalDateTime.now())) {
            throw new PastReservationException("이미 지난 시간으로는 예약 대기를 신청할 수 없습니다.");
        }
    }

    private void validateNotDuplicateWaiting(ReservationWaiting waiting) {
        if (reservationWaitingRepository.existsByNameWith(
                waiting.getName(), waiting.getDate(), waiting.getTime().getId(), waiting.getTheme().getId())) {
            throw new DuplicateReservationException("이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private void validateUpdatableReservation(ReservationWaiting waiting, String name) {
        validateOwner(waiting, name);
        validateReservationNotLocked(waiting);
    }

    private void validateOwner(ReservationWaiting waiting, String name) {
        if (!waiting.isOwnedBy(name)) {
            throw new ForbiddenReservationException("본인의 예약 대기만 취소할 수 있습니다.");
        }
    }

    private void validateReservationNotLocked(ReservationWaiting waiting) {
        if (waiting.isPast()) {
            throw new PastReservationLockedException("이미 지난 예약 대기는 취소할 수 없습니다.");
        }
    }
}
