package roomescape.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.WaitingList;
import roomescape.domain.ReservationStatus;
import roomescape.dto.command.WaitingListDeleteCommand;
import roomescape.dto.request.WaitingListCreateRequest;
import roomescape.dto.response.WaitingListResult;
import roomescape.repository.WaitingListRow;
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

    private static final String NAME = "name";
    private static final Long THEME_ID = 1L;
    private static final Long TIME_ID = 1L;
    private final Theme theme = Theme.createWithId(THEME_ID, "테스트용", "테스트용 설명", "https:");

    @Mock WaitingListRepository waitingListRepository;
    @Mock ThemeRepository themeRepository;
    @Mock ReservationTimeRepository reservationTimeRepository;
    @Mock ReservationRepository reservationRepository;

    @InjectMocks WaitingListService waitingListService;

    private WaitingListCreateRequest createCommand(LocalDate date) {
        return new WaitingListCreateRequest(NAME, date, TIME_ID, THEME_ID);
    }

    private WaitingListDeleteCommand deleteCommand(Long waitingListId) {
        return new WaitingListDeleteCommand(waitingListId, NAME);
    }

    @Nested
    class 예약대기_생성 {

        @Test
        void 성공() {
            // given
            LocalDate date = LocalDate.now().plusDays(3);
            LocalTime startAt = LocalTime.of(10, 0);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, startAt, startAt.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, date, TIME_ID, THEME_ID)).willReturn(false);
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, TIME_ID, THEME_ID)).willReturn(true);
            given(waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, date, TIME_ID, THEME_ID)).willReturn(false);
            given(waitingListRepository.save(argThat(w ->
                    w.getName().equals(NAME) &&
                    w.getReservationDate().getDate().equals(date) &&
                    w.getReservationTime().getId().equals(TIME_ID) &&
                    w.getTheme().getId().equals(THEME_ID)
            ))).willReturn(WaitingList.create(NAME, date, theme, reservationTime).withId(1));
            given(waitingListRepository.findWaitingOrderByDateAndTimeIdAndThemeId(argThat(w ->
                    w.getName().equals(NAME) &&
                    w.getReservationDate().getDate().equals(date)
            ))).willReturn(1);

            // when
            WaitingListResult result = waitingListService.create(createCommand(date));

            // then
            assertThat(result.waitingOrder()).isEqualTo(1);
            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.date()).isEqualTo(date);
            assertThat(result.timeId()).isEqualTo(TIME_ID);
            assertThat(result.themeId()).isEqualTo(THEME_ID);
        }

        @Test
        void 오늘_미래_시간으로_성공() {
            // given
            LocalDate today = LocalDate.now();
            LocalTime futureTime = LocalTime.now().plusHours(1);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, futureTime, futureTime.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, today, TIME_ID, THEME_ID)).willReturn(false);
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(today, TIME_ID, THEME_ID)).willReturn(true);
            given(waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, today, TIME_ID, THEME_ID)).willReturn(false);
            given(waitingListRepository.save(argThat(w ->
                    w.getName().equals(NAME) &&
                    w.getReservationDate().getDate().equals(today) &&
                    w.getReservationTime().getId().equals(TIME_ID) &&
                    w.getTheme().getId().equals(THEME_ID)
            ))).willReturn(WaitingList.create(NAME, today, theme, reservationTime).withId(1));
            given(waitingListRepository.findWaitingOrderByDateAndTimeIdAndThemeId(argThat(w ->
                    w.getName().equals(NAME) &&
                    w.getReservationDate().getDate().equals(today)
            ))).willReturn(1);

            // when
            WaitingListResult result = waitingListService.create(createCommand(today));

            // then
            assertThat(result.waitingOrder()).isEqualTo(1);
            assertThat(result.name()).isEqualTo(NAME);
            assertThat(result.date()).isEqualTo(today);
            assertThat(result.timeId()).isEqualTo(TIME_ID);
            assertThat(result.themeId()).isEqualTo(THEME_ID);
        }

        @Test
        void 테마가_없으면_예외발생() {
            // given
            given(themeRepository.findById(THEME_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(LocalDate.now().plusDays(3))))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_NOT_FOUND);
            verify(waitingListRepository, never()).save(any(WaitingList.class));
        }

        @Test
        void 시간이_없으면_예외발생() {
            // given
            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(LocalDate.now().plusDays(3))))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_NOT_FOUND);
            verify(waitingListRepository, never()).save(any(WaitingList.class));
        }

        @Test
        void 과거_날짜면_예외발생() {
            // given
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalTime startAt = LocalTime.of(10, 0);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, startAt, startAt.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(yesterday)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
        }

        @Test
        void 오늘_과거_시간이면_예외발생() {
            // given
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.of(0, 0), LocalTime.of(0, 30));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(LocalDate.now())))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
        }

        @Test
        void 오늘_현재_시간이면_예외발생() {
            // given
            LocalTime now = LocalTime.now();
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, now, now.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(LocalDate.now())))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
        }

        @Test
        void 본인이_이미_예약한_슬롯이면_예외발생() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startAt = LocalTime.of(10, 0);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, startAt, startAt.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, date, TIME_ID, THEME_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(date)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_RESERVED_BY_SELF);
            verify(waitingListRepository, never()).save(any(WaitingList.class));
        }

        @Test
        void 이미_대기중이면_예외발생() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            LocalTime startAt = LocalTime.of(10, 0);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, startAt, startAt.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, TIME_ID, THEME_ID)).willReturn(true);
            given(waitingListRepository.existsByNameAndDateAndTimeIdAndThemeId(NAME, date, TIME_ID, THEME_ID)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(date)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_ON_WAITING_LIST);
        }

        @Test
        void 예약_가능한_상태면_예외발생() {
            // given
            LocalDate date = LocalDate.now().plusDays(3);
            LocalTime startAt = LocalTime.of(10, 0);
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, startAt, startAt.plusHours(1));

            given(themeRepository.findById(THEME_ID)).willReturn(Optional.of(theme));
            given(reservationTimeRepository.findById(TIME_ID)).willReturn(Optional.of(reservationTime));
            given(reservationRepository.existsByDateAndTimeIdAndThemeId(date, TIME_ID, THEME_ID)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> waitingListService.create(createCommand(date)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_REQUIRED);
        }

    }

    @Nested
    class 예약대기_삭제 {

        @Test
        void 성공() {
            // given
            Long waitingListId = 1L;
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.of(10, 0), LocalTime.of(11, 0));
            WaitingList waitingList = WaitingList.createWithId(waitingListId, NAME, LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));

            given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

            // when
            waitingListService.delete(deleteCommand(waitingListId));

            // then
            verify(waitingListRepository).deleteById(waitingListId);
        }

        @Test
        void 존재하지_않으면_예외발생() {
            // given
            Long waitingListId = 1L;
            given(waitingListRepository.findById(waitingListId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> waitingListService.delete(deleteCommand(waitingListId)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WAITING_LIST_NOT_FOUND);
            verify(waitingListRepository, never()).deleteById(waitingListId);
        }

        @Test
        void 사용자명_불일치시_예외발생() {
            // given
            Long waitingListId = 1L;
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.of(10, 0), LocalTime.of(11, 0));
            WaitingList waitingList = WaitingList.createWithId(waitingListId, "다른사람", LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));

            given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

            // when & then
            assertThatThrownBy(() -> waitingListService.delete(deleteCommand(waitingListId)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NAME_NOT_MATCHED);
            verify(waitingListRepository, never()).deleteById(waitingListId);
        }

        @Test
        void 과거_날짜면_예외발생() {
            // given
            Long waitingListId = 1L;
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.of(10, 0), LocalTime.of(11, 0));
            WaitingList waitingList = WaitingList.createWithId(waitingListId, NAME, LocalDate.now().minusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));

            given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

            // when & then
            assertThatThrownBy(() -> waitingListService.delete(deleteCommand(waitingListId)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DATE_ALREADY_PASSED);
            verify(waitingListRepository, never()).deleteById(waitingListId);
        }

        @Test
        void 과거_시간이면_예외발생() {
            // given
            Long waitingListId = 1L;
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.of(0, 0), LocalTime.of(0, 30));
            WaitingList waitingList = WaitingList.createWithId(waitingListId, NAME, LocalDate.now(), theme, reservationTime, LocalDateTime.now().minusDays(1));

            given(waitingListRepository.findById(waitingListId)).willReturn(Optional.of(waitingList));

            // when & then
            assertThatThrownBy(() -> waitingListService.delete(deleteCommand(waitingListId)))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TIME_ALREADY_PASSED);
            verify(waitingListRepository, never()).deleteById(waitingListId);
        }
    }

    @Nested
    class 예약대기_조회 {

        @Test
        void 사용자명으로_목록_조회() {
            // given
            ReservationTime reservationTime = ReservationTime.createWithId(TIME_ID, LocalTime.now().plusHours(1), LocalTime.now().plusHours(2));
            WaitingList waitingList = WaitingList.createWithId(1L, NAME, LocalDate.now().plusDays(1), theme, reservationTime, LocalDateTime.now().minusDays(1));
            WaitingListRow waitingListRow = new WaitingListRow(waitingList, 1);

            given(waitingListRepository.findByName(NAME)).willReturn(List.of(waitingListRow));

            // when
            List<WaitingListResult> responses = waitingListService.getWaitingListByName(NAME);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.getFirst().name()).isEqualTo(NAME);
            assertThat(responses.getFirst().status()).isEqualTo(ReservationStatus.WAITING_LIST);
        }

        @Test
        void 없는_사용자명이면_빈_목록_반환() {
            // given
            given(waitingListRepository.findByName(NAME)).willReturn(List.of());

            // when
            List<WaitingListResult> responses = waitingListService.getWaitingListByName(NAME);

            // then
            assertThat(responses).isEmpty();
        }
    }
}
