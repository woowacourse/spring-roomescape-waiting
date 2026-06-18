package roomescape.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaReservationWaitingRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.result.ReservationWaitingOrderResult;
import roomescape.service.result.WaitingResult;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {
    private final JpaReservationWaitingRepository reservationWaitingRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final ReservationWaitingValidator reservationWaitingValidator;

    public ReservationWaitingService(
            JpaReservationWaitingRepository reservationWaitingRepository,
            JpaReservationTimeRepository reservationTimeRepository,
            JpaThemeRepository themeRepository,
            ReservationWaitingValidator reservationWaitingValidator
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationWaitingValidator = reservationWaitingValidator;
    }

    public List<WaitingResult> findByName(String name) {
        List<ReservationWaiting> waitings = reservationWaitingRepository.findByName(name);
        Map<Long, Long> turns = calculateTurns(reservationWaitingRepository.findOrderResultsBy(waitings));

        return waitings.stream()
                .map(waiting -> new WaitingResult(
                        waiting.getId(),
                        waiting.getName(),
                        waiting.getDate(),
                        waiting.getTime(),
                        waiting.getTheme(),
                        turns.get(waiting.getId())))
                .toList();
    }

    @Transactional
    public WaitingResult create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);
        ReservationWaiting waiting = new ReservationWaiting(null, name, date, time, theme);

        reservationWaitingValidator.validateWaiting(waiting);

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
        reservationWaitingValidator.validateUpdatableReservation(waiting, name);
        reservationWaitingRepository.deleteById(id);
    }

    private long calculateTurn(ReservationWaiting waiting) {
        return reservationWaitingRepository.countEarlierWaitings(waiting.getId()) + 1;
    }

    private Map<Long, Long> calculateTurns(List<ReservationWaitingOrderResult> orderResults) {
        Map<WaitingSlot, List<ReservationWaitingOrderResult>> grouped = orderResults.stream()
                .collect(Collectors.groupingBy(result ->
                        new WaitingSlot(result.date(), result.timeId(), result.themeId())));

        Map<Long, Long> turns = new HashMap<>();
        grouped.values().forEach(group -> putTurns(turns, group));
        return turns;
    }

    private void putTurns(Map<Long, Long> turns, List<ReservationWaitingOrderResult> group) {
        group.sort(Comparator
                .comparing(ReservationWaitingOrderResult::createdAt)
                .thenComparing(ReservationWaitingOrderResult::id));

        for (int index = 0; index < group.size(); index++) {
            turns.put(group.get(index).id(), (long) index + 1);
        }
    }

    @NonNull
    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    @NonNull
    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 시간입니다."));
    }

    @NonNull
    private ReservationWaiting save(ReservationWaiting waiting) {
        try {
            return reservationWaitingRepository.save(waiting);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION, "이미 예약 대기를 신청한 시간입니다.");
        }
    }

    @NonNull
    private ReservationWaiting findWaiting(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 대기입니다."));
    }

}
