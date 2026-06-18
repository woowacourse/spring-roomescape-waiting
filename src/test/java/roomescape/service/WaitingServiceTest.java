package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static roomescape.common.config.FixedClockConfig.TODAY;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.common.config.FixedClockConfig;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.UserName;
import roomescape.domain.slot.EventSlot;
import roomescape.domain.slot.theme.Description;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.theme.ThemeName;
import roomescape.domain.slot.theme.ThumbnailUrl;
import roomescape.domain.slot.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
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

    private final EventSlot eventSlot = new EventSlot(date, time, theme);

    private final Clock fixedClock = new FixedClockConfig().testClock();
    private final LocalDateTime createAt = LocalDateTime.now(fixedClock);

    private final Long waitingId = 1L;

    private final WaitingCommand command = new WaitingCommand(userName, date, timeId, themeId);

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
    void setUp() {
        waitingService = new WaitingService(this.reservationDao, this.waitingDao, this.reservationTimeDao,
                this.themeDao, this.fixedClock);
    }

    @Test
    @DisplayName("예약 대기를 등록한다.")
    public void registerWaiting_Success() {
        Waiting saved = new Waiting(
                waitingId,
                UserName.parse(userName),
                eventSlot,
                createAt
        );

        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(waitingDao.save(any())).willReturn(saved);
        given(reservationDao.existsBySlot(eventSlot)).willReturn(true);

        WaitingResult result = waitingService.registerWaiting(command);
        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo(saved.getName().value());
        assertThat(result.date()).isEqualTo(saved.getSlot().date());
        assertThat(result.timeResult()).isEqualTo(ReservationTimeResult.from(saved.getSlot().time()));
        assertThat(result.themeResult()).isEqualTo(ThemeResult.from(saved.getSlot().theme()));
        assertThat(result.createdAt()).isEqualTo(saved.getCreatedAt());
    }

    @Test
    @DisplayName("예약 대기 생성 시 시간이 존재하지 않으면 예외가 발생한다.")
    void registerWaiting_WhenTimeNotFound_ThrowsNotFoundException() {
        given(reservationTimeDao.findById(timeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.registerWaiting(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 테마가 존재하지 않으면 예외가 발생한다.")
    void registerWaiting_WhenThemeNotFound_ThrowsNotFoundException() {
        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.registerWaiting(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 예약이 없으면 예외가 발생한다.")
    void registerWaiting_WhenNoReservation_ThrowsUnprocessableEntityException() {
        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBySlot(eventSlot)).willReturn(false);

        assertThatThrownBy(() -> waitingService.registerWaiting(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("예약이 존재하지 않으면 예약 대기를 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 본인이 예약한 시간이면 예외가 발생한다.")
    void registerWaiting_WhenAlreadyReservedByUser_ThrowsUnprocessableEntityException() {
        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBySlot(eventSlot)).willReturn(true);
        given(reservationDao.existsByUserNameAndSlot(userName, eventSlot)).willReturn(true);

        assertThatThrownBy(() -> waitingService.registerWaiting(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("본인이 이미 예약한 시간에는 대기를 신청할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 대기 생성 시 중복으로 신청하면 예외가 발생한다.")
    void registerWaiting_WhenDuplicateWaiting_ThrowsConflictException() {
        given(reservationTimeDao.findById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBySlot(eventSlot)).willReturn(true);
        given(waitingDao.existsByUserNameAndSlot(userName, eventSlot)).willReturn(true);

        assertThatThrownBy(() -> waitingService.registerWaiting(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("예약 대기는 중복으로 생성할 수 없습니다.");
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void deleteWaiting_Success() {
        Waiting origin = new Waiting(waitingId, UserName.parse(userName), date, time, theme, createAt);

        given(waitingDao.findById(waitingId)).willReturn(Optional.of(origin));

        assertDoesNotThrow(() -> waitingService.deleteWaiting(waitingId, userName));
    }

    @Test
    @DisplayName("예약 대기 삭제 시 대기가 존재하지 않으면 예외가 발생한다.")
    void deleteWaiting_WhenWaitingNotFound_ThrowsNotFoundException() {
        given(waitingDao.findById(waitingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> waitingService.deleteWaiting(waitingId, userName))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약 대기가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("예약 대기 삭제 시 다른 사람의 대기를 삭제하면 예외가 발생한다.")
    void deleteWaiting_WhenNotOwner_ThrowsForbiddenException() {
        Waiting origin = new Waiting(waitingId, UserName.parse(userName), date, time, theme, createAt);

        given(waitingDao.findById(waitingId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> waitingService.deleteWaiting(waitingId, "다른 사람"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약 대기는 취소할 수 없습니다.");
    }
}
