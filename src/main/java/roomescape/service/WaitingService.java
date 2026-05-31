package roomescape.service;

import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
import roomescape.service.dto.command.WaitingCommand;
import roomescape.service.dto.result.WaitingResult;

@Service
public class WaitingService {
    private final ReservationDao reservationDao;
    private final WaitingDao waitingDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public WaitingService(
            ReservationDao reservationDao,
            WaitingDao waitingDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao,
            Clock clock
    ) {
        this.reservationDao = reservationDao;
        this.waitingDao = waitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.clock = clock;
    }

    public WaitingResult registerWaiting(WaitingCommand command) {
        ReservationTime time = reservationTimeDao.findById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Theme theme = themeDao.findThemeById(command.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        EventSlot eventSlot = EventSlot.from(command.date(), time, theme);

        eventSlot.verifyBookable(LocalDateTime.now(clock));

        validateWaitingPolicy(command.name(), eventSlot);

        Waiting waiting = new Waiting(
                UserName.parse(command.name()),
                eventSlot,
                LocalDateTime.now(clock)
        );

        Waiting saved = waitingDao.save(waiting);

        return WaitingResult.from(saved);
    }

    private void validateWaitingPolicy(String userName, EventSlot eventSlot) {
        if (!reservationDao.existsBySlot(eventSlot)) {
            throw new UnprocessableEntityException("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");
        }

        if (reservationDao.existsByUserNameAndSlot(userName, eventSlot)) {
            throw new UnprocessableEntityException("본인이 이미 예약한 시간에는 대기를 신청할 수 없습니다.");
        }

        if (waitingDao.existsByUserNameAndSlot(userName, eventSlot)) {
            throw new UnprocessableEntityException("예약 대기는 중복으로 생성할 수 없습니다.");
        }
    }

    public void deleteWaiting(Long id, String userName) {
        Waiting origin = waitingDao.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 대기가 존재하지 않습니다."));

        origin.cancel(UserName.parse(userName));

        waitingDao.delete(id);
    }
}
