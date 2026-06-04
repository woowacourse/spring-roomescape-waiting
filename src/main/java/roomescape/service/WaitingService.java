package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.WaitingResult;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final Clock clock;

    public WaitingService(ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository, WaitingRepository waitingRepository, ReservationRepository reservationRepository, Clock clock) {
        this.waitingRepository = waitingRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.clock = clock;
    }

    public List<WaitingResult> findUserWaitList(String name, int page, int size) {
        return waitingRepository.findUserWaitingList(name, page, size).stream()
                .map(waiting -> WaitingResult.of(
                        waiting, waitingRepository.findWaitingOrder(waiting)))
                .toList();
    }

    @Transactional
    public WaitingResult createWaiting(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Waiting waiting = Waiting.create(name, new Schedule(date, time, theme), LocalDateTime.now(clock));

        checkDuplicatedWaiting(waiting);
        checkWaitable(waiting);

        Waiting saved = save(waiting);
        Long order = waitingRepository.findWaitingOrder(saved);

        return WaitingResult.of(saved, order);
    }

    @Transactional
    public void deleteWaiting(Long id, String name) {
        Waiting waiting = waitingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        waiting.validateCancelableBy(name);
        waitingRepository.delete(waiting);
    }

    private void checkDuplicatedWaiting(Waiting waiting) {
        boolean duplicated = waitingRepository.findByScheduleAndName(waiting).isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_WAITING);
        }
    }

    private Waiting save(Waiting waiting) {
        try {
            return waitingRepository.save(waiting);
        } catch (DuplicateKeyException e) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_WAITING);
        }
    }

    private void checkWaitable(Waiting waiting) {
        String reserverName = reservationRepository.findReserverNameByScheduleForUpdate(waiting.getSchedule())
                .orElseThrow(() -> new BusinessConflictException(ErrorCode.WAITING_WITHOUT_RESERVATION));

        if (waiting.isSameName(reserverName)) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
