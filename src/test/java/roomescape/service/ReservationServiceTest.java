package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;

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
import org.springframework.dao.DuplicateKeyException;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.config.FixedClockConfig;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.ReservationCommand;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.ReservationTimeResult;
import roomescape.service.dto.result.ThemeResult;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {
    private final String userName = "로운";
    private final LocalDate futureDate = LocalDate.parse(FUTURE_DATE);
    private final LocalDate pastDate = LocalDate.parse("2026-05-09");

    private final Long timeId = 1L;
    private final ReservationTime time = new ReservationTime(timeId, LocalTime.parse("10:00"));

    private final Long themeId = 11L;
    private final Theme theme = new Theme(
            themeId,
            ThemeName.parse("저주받은 저택"),
            Description.parse("100년 전 사라진 가문의 비밀을 파헤쳐라"),
            ThumbnailUrl.parse("/images/cursed-mansion")
    );

    private final Long reservationId = 1L;

    private final Clock fixedClock = new FixedClockConfig().testClock();

    private ReservationService reservationService;

    @Mock
    private ReservationDao reservationDao;
    @Mock
    private ReservationTimeDao reservationTimeDao;
    @Mock
    private ThemeDao themeDao;
    @Mock
    private WaitingDao waitingDao;

    @BeforeEach
    public void setUp() {
        reservationService = new ReservationService(
                reservationDao, reservationTimeDao, themeDao, waitingDao, fixedClock
        );
    }

    @Test
    public void 예약_생성_정상_테스트() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation saved = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);

        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.save(any())).willReturn(saved);

        ReservationResult result = reservationService.reserve(command);

        assertThat(result.id()).isEqualTo(saved.getId());
        assertThat(result.name()).isEqualTo(saved.getName().value());
        assertThat(result.date()).isEqualTo(saved.getDate());
        assertThat(result.timeResult()).isEqualTo(ReservationTimeResult.from(saved.getTime()));
        assertThat(result.themeResult()).isEqualTo(ThemeResult.from(saved.getTheme()));
    }

    @Test
    public void 존재하지_않는_시간으로_예약하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 시간입니다.");

        verify(reservationDao, never()).save(any());
    }

    @Test
    public void 존재하지_않는_테마로_예약하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 테마입니다.");

        verify(reservationDao, never()).save(any());
    }

    @Test
    public void 과거_시간으로_예약하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, pastDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 시간입니다.");

        verify(reservationDao, never()).save(any());
    }

    @Test
    public void 중복된_슬롯에_예약하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBy(futureDate, theme, time)).willReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 존재하는 예약 건입니다.");

        verify(reservationDao, never()).save(any());
    }

    @Test
    public void 대기가_있는_슬롯에_예약하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(waitingDao.existsBySlot(futureDate, timeId, themeId)).willReturn(true);

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약 대기자가 있는 시간입니다, 예약 대기로 신청해주세요.");

        verify(reservationDao, never()).save(any());
    }

    @Test
    public void 다른_사용자가_먼저_같은_슬롯을_예약했으면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.existsBy(futureDate, theme, time)).willReturn(false);
        given(waitingDao.existsBySlot(futureDate, timeId, themeId)).willReturn(false);
        given(reservationDao.save(any())).willThrow(new DuplicateKeyException("UNIQUE"));

        assertThatThrownBy(() -> reservationService.reserve(command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약된 시간입니다. 다시 시도해주세요.");
    }

    @Test
    public void 예약_변경_정상_테스트() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.update(any())).willReturn(true);

        ReservationResult result = reservationService.changeReservationSlot(reservationId, command);

        assertThat(result.id()).isEqualTo(reservationId);
        assertThat(result.name()).isEqualTo(userName);
        assertThat(result.date()).isEqualTo(futureDate);
        verify(reservationDao).update(any());
    }

    @Test
    public void 존재하지_않는_예약을_변경하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        given(reservationDao.findById(reservationId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약이 존재하지 않습니다.");

        verify(reservationDao, never()).update(any());
    }

    @Test
    public void 타인의_예약을_변경하면_예외가_발생한다() {
        String otherUser = "다른사람";
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(otherUser), futureDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약은 취소/변경할 수 없습니다.");

        verify(reservationDao, never()).update(any());
    }

    @Test
    public void 지나간_예약을_변경하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), pastDate, time, theme);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 시간입니다.");

        verify(reservationDao, never()).update(any());
    }

    @Test
    public void 예약_변경_저장에_실패하면_충돌_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.update(any())).willReturn(false);

        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약된 시간입니다. 다시 시도해주세요.");
    }

    @Test
    public void 대기가_있는_슬롯으로_예약을_변경하면_예외가_발생한다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(waitingDao.existsBySlot(futureDate, timeId, themeId)).willReturn(true);

        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(ConflictException.class)
                .hasMessage("이미 예약 대기자가 있는 시간입니다, 예약 대기로 신청해주세요.");

        verify(reservationDao, never()).update(any());
    }

    @Test
    public void 예약_취소_정상_테스트() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));

        reservationService.cancelReservation(reservationId, userName);

        verify(reservationDao).delete(reservationId);
    }

    @Test
    public void 존재하지_않는_예약을_취소하면_예외가_발생한다() {
        given(reservationDao.findById(reservationId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, userName))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("삭제하려는 예약이 존재하지 않습니다.");

        verify(reservationDao, never()).delete(anyLong());
    }

    @Test
    public void 타인의_예약을_취소하면_예외가_발생한다() {
        String otherUser = "다른사람";
        Reservation origin = new Reservation(reservationId, UserName.parse(otherUser), futureDate, time, theme);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, userName))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("다른 사람의 예약은 취소/변경할 수 없습니다.");

        verify(reservationDao, never()).delete(anyLong());
    }

    @Test
    public void 과거_예약을_취소하면_예외가_발생한다() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), pastDate, time, theme);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));

        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, userName))
                .isInstanceOf(UnprocessableEntityException.class)
                .hasMessage("이미 지난 시간입니다.");

        verify(reservationDao, never()).delete(anyLong());
    }

    @Test
    public void 관리자_예약_삭제_정상_테스트() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);
        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        reservationService.removeReservation(reservationId);

        verify(reservationDao).delete(reservationId);
    }

    @Test
    public void 예약_취소_시_같은_슬롯의_1번_대기자가_예약으로_승격된다() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);
        Long waitingId = 99L;
        Waiting firstWaiting = new Waiting(
                waitingId,
                UserName.parse("대기자"),
                futureDate, time, theme,
                LocalDateTime.of(2026, 5, 9, 12, 0)
        );

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationDao.delete(reservationId)).willReturn(true);
        given(waitingDao.findFirstBySlot(futureDate, timeId, themeId)).willReturn(Optional.of(firstWaiting));

        reservationService.cancelReservation(reservationId, userName);

        verify(reservationDao).delete(reservationId);
        verify(reservationDao).save(any(Reservation.class));
        verify(waitingDao).delete(waitingId);
    }

    @Test
    public void 예약_취소_시_대기자가_없으면_승격이_일어나지_않는다() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationDao.delete(reservationId)).willReturn(true);
        given(waitingDao.findFirstBySlot(futureDate, timeId, themeId)).willReturn(Optional.empty());

        reservationService.cancelReservation(reservationId, userName);

        verify(reservationDao).delete(reservationId);
        verify(reservationDao, never()).save(any());
        verify(waitingDao, never()).delete(anyLong());
    }

    @Test
    public void 관리자_강제_삭제_시_미래_슬롯이면_1번_대기자가_예약으로_승격된다() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);
        Long waitingId = 99L;
        Waiting firstWaiting = new Waiting(
                waitingId,
                UserName.parse("대기자"),
                futureDate, time, theme,
                LocalDateTime.of(2026, 5, 9, 12, 0)
        );

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationDao.delete(reservationId)).willReturn(true);
        given(waitingDao.findFirstBySlot(futureDate, timeId, themeId)).willReturn(Optional.of(firstWaiting));

        reservationService.removeReservation(reservationId);

        verify(reservationDao).delete(reservationId);
        verify(reservationDao).save(any(Reservation.class));
        verify(waitingDao).delete(waitingId);
    }

    @Test
    public void 관리자_강제_삭제_시_과거_슬롯이면_승격이_일어나지_않는다() {
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), pastDate, time, theme);

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationDao.delete(reservationId)).willReturn(true);
        reservationService.removeReservation(reservationId);

        verify(reservationDao).delete(reservationId);
        verify(waitingDao, never()).findFirstBySlot(any(), anyLong(), anyLong());
        verify(reservationDao, never()).save(any());
        verify(waitingDao, never()).delete(anyLong());
    }

    @Test
    public void 예약_변경_시_변경_전_슬롯의_1번_대기자가_예약으로_승격된다() {
        ReservationCommand command = new ReservationCommand(userName, futureDate, timeId, themeId);
        Reservation origin = new Reservation(reservationId, UserName.parse(userName), futureDate, time, theme);
        Long waitingId = 99L;
        Waiting firstWaiting = new Waiting(
                waitingId,
                UserName.parse("대기자"),
                futureDate, time, theme,
                LocalDateTime.of(2026, 5, 9, 12, 0)
        );

        given(reservationDao.findById(reservationId)).willReturn(Optional.of(origin));
        given(reservationTimeDao.findTimeById(timeId)).willReturn(Optional.of(time));
        given(themeDao.findThemeById(themeId)).willReturn(Optional.of(theme));
        given(reservationDao.update(any())).willReturn(true);
        given(waitingDao.findFirstBySlot(futureDate, timeId, themeId)).willReturn(Optional.of(firstWaiting));

        reservationService.changeReservationSlot(reservationId, command);

        verify(reservationDao).update(any());
        verify(reservationDao).save(any(Reservation.class));
        verify(waitingDao).delete(waitingId);
    }
}