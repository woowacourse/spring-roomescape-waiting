package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateReservationException;
import roomescape.exception.NotFoundException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.dto.WaitingWithTurn;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationWaitingValidator reservationWaitingValidator;

    public ReservationWaitingService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            ReservationWaitingValidator reservationWaitingValidator
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationWaitingValidator = reservationWaitingValidator;
    }

    public List<WaitingResult> findByName(String name) {
        return reservationWaitingRepository.findByNameWithTurn(name).stream()
                .map(waitingWithTurn -> {
                    ReservationWaiting waiting = waitingWithTurn.waiting();
                    return new WaitingResult(
                            waiting.getId(),
                            waiting.getName(),
                            waiting.getDate(),
                            waiting.getTime(),
                            waiting.getTheme(),
                            waitingWithTurn.turn());
                }).toList();
    }

    @Transactional
    public WaitingResult create(String name, LocalDate date, Long timeId, Long themeId) {
        ReservationTime time = findReservationTime(timeId);
        Theme theme = findTheme(themeId);
        ReservationWaiting waiting = new ReservationWaiting(null, name, date, time, theme);

        reservationWaitingValidator.validateWaiting(waiting);

        WaitingWithTurn saved = save(waiting);
        ReservationWaiting savedWaiting = saved.waiting();
        return new WaitingResult(
                savedWaiting.getId(),
                savedWaiting.getName(),
                savedWaiting.getDate(),
                savedWaiting.getTime(),
                savedWaiting.getTheme(),
                saved.turn());
    }

    @Transactional
    public void delete(Long id, String name) {
        ReservationWaiting waiting = findWaiting(id);
        reservationWaitingValidator.validateUpdatableReservation(waiting, name);
        reservationWaitingRepository.delete(id);
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findBy(themeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findBy(timeId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));
    }

    private WaitingWithTurn save(ReservationWaiting waiting) {
        try {
            Long id = reservationWaitingRepository.insert(waiting);
            return reservationWaitingRepository.findByIdWithTurn(id)
                    .orElseThrow(() -> new IllegalArgumentException("생성된 예약 대기를 찾을 수 없습니다."));
        } catch (DuplicateKeyException e) {
            throw new DuplicateReservationException("이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private ReservationWaiting findWaiting(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 예약 대기입니다."));
    }
}
