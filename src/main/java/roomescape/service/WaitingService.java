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
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;
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

    public WaitingResult save(WaitingCommand command) {
        ReservationTime time = reservationTimeDao.findTimeById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Theme theme = themeDao.findThemeById(command.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        if (!reservationDao.existsBy(command.date(), theme, time)) {
            throw new UnprocessableEntityException("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");
        }

        if (reservationDao.existsByUserNameAndSlot(command.name(), command.date(), theme, time)) {
            throw new UnprocessableEntityException("본인이 이미 예약한 시간에는 대기를 신청할 수 없습니다.");
        }

        Waiting waiting = new Waiting(
                UserName.parse(command.name()),
                command.date(),
                time,
                theme,
                LocalDateTime.now(clock)
        );

        if (waitingDao.existsBySlotAndName(waiting.getName().value(), waiting.getDate(), time.getId(), theme.getId())) {
            throw new UnprocessableEntityException("예약 대기는 중복으로 생성할 수 없습니다.");
        }

        Waiting saved = waitingDao.save(waiting);

        return WaitingResult.from(saved);
    }

    public void delete(Long id, String userName) {
        Waiting origin = waitingDao.findById(id)
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 대기가 존재하지 않습니다."));

        origin.validateOwner(userName);

        waitingDao.delete(id);
    }
}