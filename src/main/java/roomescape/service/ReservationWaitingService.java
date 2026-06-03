package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    public List<WaitingWithTurn> findByName(String name) {
        return reservationWaitingRepository.findByNameWithTurn(name);
    }

    @Transactional
    public WaitingWithTurn create(String name, LocalDate date, Long timeId, Long themeId, LocalDateTime now) {
        ReservationSlot slot = new ReservationSlot(date, findReservationTime(timeId), findTheme(themeId));
        ReservationWaiting waiting = new ReservationWaiting(null, name, slot);
        reservationWaitingValidator.validateWaiting(waiting, now);

        return insertReservationWaiting(waiting);
    }

    @Transactional
    public void delete(Long id, String name, LocalDateTime now) {
        ReservationWaiting waiting = findWaiting(id);
        reservationWaitingValidator.validateModifiable(waiting, name, now);
        reservationWaitingRepository.delete(id);
    }

    private ReservationTime findReservationTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 시간입니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 테마입니다."));
    }

    private WaitingWithTurn insertReservationWaiting(ReservationWaiting waiting) {
        try {
            ReservationWaiting savedWaiting = reservationWaitingRepository.insert(waiting);
            return reservationWaitingRepository.findByIdWithTurn(savedWaiting.getId())
                    .orElseThrow(() -> new IllegalStateException("생성된 예약 대기를 찾을 수 없습니다."));
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESOURCE, "이미 예약 대기를 신청한 시간입니다.");
        }
    }

    private ReservationWaiting findWaiting(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.NOT_FOUND, "존재하지 않는 예약 대기입니다."));
    }
}
