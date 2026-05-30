package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.config.FixedClockConfig;
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

    private final Clock fixedClock = new FixedClockConfig().testClock();
    private final LocalDateTime createAt = LocalDateTime.now(fixedClock);

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
        waitingService = new WaitingService(this.reservationDao, this.waitingDao, this.reservationTimeDao,
                this.themeDao, this.fixedClock);
    }

    @Test
    public void 예약_대기_생성_정상_테스트() {
        WaitingCommand command = new WaitingCommand(
                userName,
                date,
                timeId,
                themeId
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

    @Test
    public void 존재하지_않는_시간으로_대기를_신청하면_예외가_발생한다() {
        WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다.");

        verify(waitingDao, never()).save(any());
    }

    @Test
    public void 존재하지_않는_테마로_대기를_신청하면_예외가_발생한다() {
        WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");

        verify(waitingDao, never()).save(any());
    }

    @Test
    public void 예약이_없는_슬롯에_대기를_신청하면_예외가_발생한다() {
        WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBy(date, theme, time)).willReturn(false);

        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");

        verify(waitingDao, never()).save(any());
    }

    @Test
    public void 본인이_이미_예약한_시간에_대기를_신청하면_예외가_발생한다() {
        WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBy(date, theme, time)).willReturn(true);
        given(reservationDao.existsByUserNameAndSlot(userName, date, theme, time)).willReturn(true);

        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("본인이 이미 예약한 시간에는 대기를 신청할 수 없습니다.");

        verify(waitingDao, never()).save(any());
    }

    @Test
    public void 같은_슬롯에_대기를_중복으로_신청하면_예외가_발생한다() {
        WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBy(date, theme, time)).willReturn(true);
        given(waitingDao.existsBySlotAndName(anyString(), any(LocalDate.class), anyLong(), anyLong())).willReturn(true);

        assertThatThrownBy(() -> waitingService.save(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("예약 대기는 중복으로 생성할 수 없습니다.");

        verify(waitingDao, never()).save(any());
    }

    @Test
    public void 예약_대기_삭제_정상_테스트() {
        Waiting origin = new Waiting(
                waitingId,
                UserName.parse(userName),
                date,
                time,
                theme,
                createAt
        );

        given(waitingDao.findById(waitingId)).willReturn(Optional.of(origin));
        waitingService.delete(waitingId, userName);
        verify(waitingDao).delete(waitingId);
    }

    @Test
    public void 존재하지_않는_예약_대기를_삭제하면_예외가_발생한다() {
        given(waitingDao.findById(waitingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.delete(waitingId, userName))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약 대기가 존재하지 않습니다.");

        verify(waitingDao, never()).delete(anyLong());
    }

    @Test
    public void 타인의_예약_대기를_삭제하면_예외가_발생한다() {
        String otherUser = "다른사람";
        Waiting origin = new Waiting(
                waitingId,
                UserName.parse(otherUser),
                date,
                time,
                theme,
                createAt
        );
        given(waitingDao.findById(waitingId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> waitingService.delete(waitingId, userName))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약 대기는 취소할 수 없습니다.");

        verify(waitingDao, never()).delete(anyLong());
    }
}