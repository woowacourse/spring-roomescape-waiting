package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.config.FixedClockConfig.NOW_TIME;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.WaitingCommand;
import roomescape.service.dto.result.ReservationTimeResult;
import roomescape.service.dto.result.ThemeResult;
import roomescape.service.dto.result.WaitingResult;

@ExtendWith(MockitoExtension.class)
class WaitingServiceTest {
    private final String userName = "로운";
    private final LocalDate date = LocalDate.parse(TODAY);

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 11L;
    private final ThemeName themeName = ThemeName.parse("저주받은 저택");
    private final Description description = Description.parse("100년 전 사라진 가문의 비밀을 파헤쳐라");
    private final ThumbnailUrl url = ThumbnailUrl.parse("/images/cursed-mansion");
    private final Theme theme = new Theme(themeId, themeName, description, url);

    private final LocalDateTime createAt = LocalDateTime.parse(TODAY + "T" + NOW_TIME);

    private final Long waitingId = 1L;

    private WaitingService waitingService;

    @Mock
    private ReservationDao reservationDao;

    @Mock
    private WaitingDao waitingDao;
    @Mock
    private ReservationTimeDao reservationTimeDao;
    @Mock
    private ThemeDao themeDao;

    @BeforeEach
    public void setUp() {
        waitingService = new WaitingService(this.reservationDao, this.waitingDao, this.reservationTimeDao, this.themeDao);
    }

    @Test
    public void 예약_생성_정상_테스트() {
        WaitingCommand command = new WaitingCommand(
                userName,
                date,
                timeId,
                themeId,
                createAt
        );
        Waiting saved = new Waiting(
                waitingId,
                UserName.parse(userName),
                date,
                time,
                theme,
                createAt
        );

        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(waitingDao.save(any())).willReturn(saved);
        given(reservationDao.existsBy(date, theme, time)).willReturn(true);

        WaitingResult result = waitingService.save(command);
        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo(saved.getName().value());
        assertThat(result.date()).isEqualTo(saved.getDate());
        assertThat(result.timeResult()).isEqualTo(ReservationTimeResult.from(saved.getTime()));
        assertThat(result.themeResult()).isEqualTo(ThemeResult.from(saved.getTheme()));
        assertThat(result.createdAt()).isEqualTo(saved.getCreatedAt());
    }
}
