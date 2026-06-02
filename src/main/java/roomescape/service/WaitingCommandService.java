package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.*;
import roomescape.domain.theme.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.service.command.WaitingCommand;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WaitingCommandService {
    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    private ReservationTime findTimeReference(Long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(Long themeId) {
            return themeDao.findById(themeId)
                    .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }

    @Transactional
    public ReservationWaiting create(WaitingCommand command) {
        ReservationTime time = findTimeReference(command.timeId());
        Theme theme = findThemeReference(command.themeId());

        Slot slot = Slot.from(Schedule.from(command.date(), time), theme);

        Reservation reservation = reservationDao.findBySlot(slot)
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜와 시간에 예약이 존재하지 않습니다."));

        if (reservation.isOwnedBy(command.name())) {
            throw new DuplicateException("내가 예약한 시간에 예약대기를 생성할 수 없습니다.");
        }

        if (waitingDao.existsBySlotAndName(slot, command.name())) {
            throw new DuplicateException("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없습니다.");
        }

        Long savedId = waitingDao.create(ReservationWaiting.create(command.name(), slot, LocalDateTime.now(clock)));
        return waitingDao.findById(savedId)
                .orElseThrow(() -> new ResourceNotFoundException("예약 대기가 정상적으로 생성되지 않았습니다."));
    }

    @Transactional
    public void cancel(Long waitingId, UserName name) {
        ReservationWaiting waiting = waitingDao.findById(waitingId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약 대기입니다."));
        waiting.validateCancelable(LocalDateTime.now(clock));
        waiting.validateOwnedBy(name);

        waitingDao.delete(waiting);
    }
}
