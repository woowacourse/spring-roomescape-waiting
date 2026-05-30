package roomescape.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.dto.*;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingListRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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
    void 예약_대기_생성() {
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
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(true);

        WaitingList waitingList = WaitingList.create(name, date, theme, reservationTime);
        given(waitingListRepository.save(any(WaitingList.class))).willReturn(waitingList.withId(1));
        given(waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(any(WaitingList.class))).willReturn(1);

        // when
        WaitingListResult result = waitingListService.create(createCommand);

        // then
        Assertions.assertThat(result.waitingOrder()).isEqualTo(1);
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.date()).isEqualTo(date);
        Assertions.assertThat(result.timeId()).isEqualTo(timeId);
        Assertions.assertThat(result.themeId()).isEqualTo(themeId);
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
        given(themeRepository.findById(themeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
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
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));

        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.empty());

        // when && then
        Assertions.assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
        verify(waitingListRepository, never()).save(any(WaitingList.class));
    }

    @Test
    void 과거_날짜로_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate pastDate = LocalDate.now().minusDays(1);
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", pastDate, 1L, 1L);

        Long themeId = 1L;
        Long timeId = 1L;
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
    }

    @Test
    void 오늘날짜_과거시간으로_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate today = LocalDate.now();
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", today, 1L, 1L);

        Long themeId = 1L;
        Long timeId = 1L;
        LocalTime now = LocalTime.now();
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, now.minusHours(1), now.plusHours(2));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
    }

    @Test
    void 오늘날짜_현재시간으로_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate today = LocalDate.now();
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", today, 1L, 1L);

        Long themeId = 1L;
        Long timeId = 1L;
        LocalTime now = LocalTime.now();
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, now, now.plusHours(2));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
    }

    @Test
    void 오늘날짜_미래시간으로_예약대기_생성_성공() {
        // given
        String name = "김민준";
        LocalDate date = LocalDate.now();

        LocalDate today = LocalDate.now();
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand("오리", today, 1L, 1L);

        Long themeId = 1L;
        Long timeId = 1L;
        LocalTime now = LocalTime.now();
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, now.plusHours(1), now.plusHours(2));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(true);

        WaitingList waitingList = WaitingList.create(name, date, theme, reservationTime);
        given(waitingListRepository.save(any(WaitingList.class))).willReturn(waitingList.withId(1));
        given(waitingListRepository.findWaitingOrderByIdAndThemeAndDateAndTime(any(WaitingList.class))).willReturn(1);

        // when
        WaitingListResult result = waitingListService.create(createCommand);

        // then
        Assertions.assertThat(result.waitingOrder()).isEqualTo(1);
        Assertions.assertThat(result.name()).isEqualTo(name);
        Assertions.assertThat(result.date()).isEqualTo(date);
        Assertions.assertThat(result.timeId()).isEqualTo(timeId);
        Assertions.assertThat(result.themeId()).isEqualTo(themeId);
    }

    @Test
    void 같은_사용자가_여러번_예약대기_생성_시도시_예외발생() {
        // given
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String name = "오리";
        WaitingListCreateCommand createCommand = new WaitingListCreateCommand(name, tomorrow, 1L, 1L);

        Long themeId = 1L;
        Long timeId = 1L;
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(tomorrow, timeId, themeId)).willReturn(true);
        given(waitingListRepository.existsByNameAndThemeAndDateAndTime(name, themeId, tomorrow, timeId)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ON_WAITING_LIST);
    }

    @Test
    void 예약_가능한_상태에서_예약대기_생성_시도시_예외발생() {
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
        Theme theme = Theme.createWithId(themeId, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(timeId, LocalTime.of(10,0), LocalTime.of(11,0));
        given(themeRepository.findById(themeId)).willReturn(Optional.of(theme));
        given(reservationTimeRepository.findById(timeId)).willReturn(Optional.of(reservationTime));

        given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> waitingListService.create(createCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_REQUIRED);
    }

    @Test
    void 예약_대기_삭제() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when
        waitingListService.delete(deleteCommand);

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
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand))
                .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_FOUND);

        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 삭제를_시도하는_사용자명과_예약대기자명_불일치시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        WaitingList waitingList = WaitingList.createWithId(waitingListId, "검프", LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);

        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 과거_날짜_예약대기_삭제_시도시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.of(10,0), LocalTime.of(11,0));
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now().minusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);

        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 과거_시간_예약대기_삭제_시도시_예외발생() {
        // given
        String name = "김민준";
        Long waitingListId = 1L;
        WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(waitingListId, name);

        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.now().minusHours(1), LocalTime.now());
        WaitingList waitingList = WaitingList.createWithId(waitingListId, name, LocalDate.now(), theme, reservationTime, LocalDateTime.now().minusDays(1));
        given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

        // when & then
        assertThatThrownBy(() -> waitingListService.delete(deleteCommand))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);

        verify(waitingListRepository, never()).deleteById(waitingListId);
    }

    @Test
    void 사용자명으로_예약대기_목록_조회() {
        // given
        String name = "검프";
        Theme theme = Theme.createWithId(1L, "테스트용", "테스트용 설명", "https:");
        ReservationTime reservationTime = ReservationTime.createWithId(1L, LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));
        WaitingList waitingList = WaitingList.createWithId(1L, name, LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));

        given(waitingListRepository.findByName(name)).willReturn(List.of(waitingList));

        // when
        List<WaitingListResult> responses = waitingListService.getWaitingListByName(name);

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo(name);
        assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.WAITING_LIST);
    }

    @Test
    void 없는_사용자명으로_예약대기_목록_조회() {
        // given
        String name = "검프";
        given(waitingListRepository.findByName(name)).willReturn(List.of());

        // when
        List<WaitingListResult> responses = waitingListService.getWaitingListByName(name);

        // then
        assertThat(responses).hasSize(0);
    }
}
