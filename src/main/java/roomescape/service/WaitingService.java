package roomescape.service;

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

    public WaitingService(
            ReservationDao reservationDao,
            WaitingDao waitingDao,
            ReservationTimeDao reservationTimeDao,
            ThemeDao themeDao
    ) {
        this.reservationDao = reservationDao;
        this.waitingDao = waitingDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
    }

    public WaitingResult save(WaitingCommand command) {
        ReservationTime time = reservationTimeDao.findTimeById(command.timeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 시간입니다."));

        Theme theme = themeDao.findThemeById(command.themeId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다."));

        if (!reservationDao.existsBy(command.date(), theme, time)) {
            throw new UnprocessableEntityException("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");
        }

        Waiting waiting = new Waiting(
                UserName.parse(command.name()),
                command.date(),
                time,
                theme,
                command.createAt()
        );

        if (waitingDao.existsBy(waiting)) {
            throw new UnprocessableEntityException("예약 대기는 중복으로 생성할 수 없습니다.");
        }

        Waiting saved = waitingDao.save(waiting);

        return WaitingResult.from(saved);
    }
}
