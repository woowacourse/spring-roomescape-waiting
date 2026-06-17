package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;
import roomescape.service.dto.ReservationAvailableEvent;
import roomescape.service.dto.ReservationStatus;
import roomescape.service.dto.command.WaitingListCreateCommand;
import roomescape.service.dto.command.WaitingListDeleteCommand;
import roomescape.service.dto.result.WaitingListResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WaitingListServiceTest {

    @Mock
    WaitingListRepository waitingListRepository;
    @Mock
    ThemeRepository themeRepository;
    @Mock
    ReservationTimeRepository reservationTimeRepository;
    @Mock
    ReservationRepository reservationRepository;

    @InjectMocks
    WaitingListService waitingListService;

    @Test
    void 예약이_존재하는_건에_대해_예약_대기_생성() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(true);

        WaitingList waitingList = WaitingList.create(name, date, reservationTime, theme);
        given(waitingListRepository.save(any(WaitingList.class))).willReturn(waitingList.withId(1L));
        given(waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(any(WaitingList.class))).willReturn(1);

        // when
        WaitingListResult result = waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9,0));

        // then
        Assertions.assertThat(result.waitingOrder()).isEqualTo(1);
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.date()).isEqualTo(date);
        Assertions.assertThat(result.timeId()).isEqualTo(timeId);
        Assertions.assertThat(result.themeId()).isEqualTo(themeId);
    }

    @Test
    void 예약_없이_예약대기가_존재하는_건에_대해_예약_대기_생성() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(false);
        given(waitingListRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(true);

        WaitingList waitingList = WaitingList.create(name, date, reservationTime, theme);
        given(waitingListRepository.save(any(WaitingList.class))).willReturn(waitingList.withId(1L));
        given(waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(any(WaitingList.class))).willReturn(1);

        // when
        WaitingListResult result = waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9, 0));

        // then
        Assertions.assertThat(result.waitingOrder()).isEqualTo(1);
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.date()).isEqualTo(date);
        Assertions.assertThat(result.timeId()).isEqualTo(timeId);
        Assertions.assertThat(result.themeId()).isEqualTo(themeId);
    }

    @Test
    void 예약대기_신청하려는_시간이_없으면_예외발생() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );

        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand, LocalDate.now(), LocalTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
    }

    @Test
    void 예약대기_신청하려는_테마가_없으면_예외발생 () {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        given(themeRepository.findById(themeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
    }

    @Test
    void 과거_날짜로_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", pastDate, timeId, themeId);

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
    }

    @Test
    void 과거_시간으로_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate today = LocalDate.now();
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", today, timeId, themeId);

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10, 0), LocalTime.of(11, 0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand, today, LocalTime.of(12, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
    }

    @Test
    void 같은_사용자가_여러번_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String name = "오리";
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(name, tomorrow, timeId, themeId);

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(tomorrow, timeId, themeId)).willReturn(true);
        given(waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(name, tomorrow, timeId, themeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ON_WAITING_LIST);
    }

    @Test
    void 예약과_예약대기가_없는_상태에서_예약대기_생성_시도시_예외발생() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now().plusDays(3);
        Long timeId = 1L;
        Long themeId = 1L;
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(
                name,
                date,
                timeId,
                themeId
        );

        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:", 30000L);
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(false);
        given(waitingListRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_REQUIRED);
    }

    @Test
    void 예약_대기_삭제() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:", 30000L);
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));
        given(waitingListRepository.deleteById(waitingListId)).willReturn(true);

        // when
        waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.now());

        // then
        verify(waitingListRepository).deleteById(waitingListId);
    }

    @Test
    void 존재하지_않는_예약대기_삭제_시도시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.now()))
                .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_FOUND);

        // then
        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 삭제를_시도하는_사용자명과_예약대기자명_불일치시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:", 30000L);
        WaitingList waitingList = WaitingList.createWithId(waitingListId, "검프", LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.now()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);

        // then
        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 과거_날짜_예약대기_삭제_시도시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:", 30000L);
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now().minusDays(1), reservationTime, theme, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.of(9, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);

        // then
        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 과거_시간_예약대기_삭제_시도시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:", 30000L);
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now(), reservationTime, theme, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.of(12, 0)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);

        // then
        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 예약_취소_이벤트_수신시_다음_대기자가_있으면_예약으로_전환된다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationAvailableEvent event = new ReservationAvailableEvent(date, 1L, 1L);
        ReservationTime time = ReservationTime.createWithId(1L, LocalTime.of(10, 0), LocalTime.of(11, 0));
        Theme theme = Theme.createWithId(1L, "테마", "테스트 설명", "url", 30000L);
        WaitingList waiting = WaitingList.createWithId(1L, "대기자", date, time, theme, LocalDateTime.now());

        given(waitingListRepository.findFirstBySlot(date, 1L, 1L)).willReturn(Optional.of(waiting));
        given(waitingListRepository.deleteById(1L)).willReturn(true);

        // when
        waitingListService.promoteWaitingListToReservation(event);

        // then
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void 예약_취소_이벤트_수신시_다음_대기자가_없으면_아무일도_일어나지_않는다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationAvailableEvent event = new ReservationAvailableEvent(date, 1L, 1L);

        given(waitingListRepository.findFirstBySlot(date, 1L, 1L)).willReturn(Optional.empty());

        // when
        waitingListService.promoteWaitingListToReservation(event);

        // then
        verify(reservationRepository, never()).save(any(Reservation.class));
        verify(waitingListRepository, never()).deleteById(anyLong());
    }

    @Test
    void 사용자명으로_예약대기_목록_조회() {
        // given
        String name = "검프";
        LocalTime now = LocalTime.of(11, 0);
        ReservationTime reservationTime = ReservationTime.createWithId(1L, now.plusHours(1), now.plusHours(2));
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:", 30000L);
        WaitingList waitingList = WaitingList.createWithId(1L, name, LocalDate.now().plusDays(1), reservationTime, theme, LocalDateTime.now().minusDays(1));

        given(waitingListRepository.findByName(name)).willReturn(List.of(waitingList));
        given(waitingListRepository.findWaitingOrderByDateAndTimeAndTheme(waitingList)).willReturn(2);

        // when
        List<WaitingListResult> responses = waitingListService.getWaitingListByName(name);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo(name);
        assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.WAITING_LIST);
        assertThat(responses.getFirst().waitingOrder()).isEqualTo(2);
    }
}
