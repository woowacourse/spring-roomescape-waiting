package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.reservation.exception.PastReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.TimeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final LocalDateTime FUTURE_START = LocalDateTime.of(2030, 6, 1, 10, 0);
    private static final LocalDateTime FUTURE_END = LocalDateTime.of(2030, 6, 1, 12, 0);
    private static final LocalDateTime PAST_START = LocalDateTime.of(2024, 1, 1, 10, 0);
    private static final LocalDateTime PAST_END = LocalDateTime.of(2024, 1, 1, 12, 0);

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TimeService timeService;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private HolidayService holidayService;
    @Mock
    private SlotService slotService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @DisplayName("정상 입력이고 대기가 없는 경우, 저장된 예약을 반환한다.")
    @Test
    void create_정상_예약_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(holidayService.isHoliday(time.getDate())).thenReturn(false);
        when(reservationRepository.isDuplicated(1L, time)).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);

        // when
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);
        Reservation result = reservationService.create(dto);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("라이");
        assertThat(result.getStatus()).isEqualTo(Status.RESERVED);
    }

    @DisplayName("정상 입력이고 저장된 예약이 존재하는 경우, 저장된 대기 예약을 반환한다")
    @Test
    void create_예약_대기_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.WAITING, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(holidayService.isHoliday(time.getDate())).thenReturn(false);
        when(reservationRepository.isDuplicated(1L, time)).thenReturn(true);
        when(reservationRepository.save(any())).thenReturn(saved);

        // when
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);
        Reservation result = reservationService.create(dto);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("라이");
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
    }

    @DisplayName("존재하지 않는 timeId인 경우, TimeNotFoundException이 발생한다.")
    @Test
    void create_존재하지_않는_timeId이면_예외() {
        // given
        when(timeService.findById(999L)).thenThrow(new TimeNotFoundException(999L));
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 999L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("존재하지 않는 themeId인 경우, ThemeNotFoundException이 발생한다.")
    @Test
    void create_존재하지_않는_themeId이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(999L)).thenReturn(Optional.empty());
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 999L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("휴일에 예약을 시도하는 경우, IllegalArgumentException이 발생한다.")
    @Test
    void create_휴일이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(holidayService.isHoliday(time.getDate())).thenReturn(true);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("같은 사용자가 같은 슬롯에 이미 예약/대기를 가지고 있으면 같은 슬롯에 중복 신청할 수 없다.")
    @Test
    void create_같은_사용자가_같은_슬롯에_중복_신청하면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(holidayService.isHoliday(FUTURE_START.toLocalDate())).thenReturn(false);

        when(reservationRepository.isDuplicatedWithName("라이", 1L, time)).thenReturn(true);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(DuplicateReservationException.class);
    }

    @DisplayName("같은 슬롯/테마에 중복 예약을 시도하는 경우, 대기로 넘어가기 때문에 예외가 발생하지 않는다.")
    @Test
    void create_중복_예약이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.WAITING, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.findById(1L)).thenReturn(Optional.of(theme));
        when(holidayService.isHoliday(FUTURE_START.toLocalDate())).thenReturn(false);
        when(reservationRepository.isDuplicated(1L, time)).thenReturn(true);
        when(reservationRepository.isDuplicatedWithName("라이", 1L, time)).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);

        // when & then
        assertThatCode(() -> reservationService.create(dto))
                .doesNotThrowAnyException();
    }

    @DisplayName("지나간 시간 슬롯에 예약을 시도하는 경우, PastReservationException이 발생한다.")
    @Test
    void create_과거_슬롯_예약이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);
        when(timeService.findById(1L)).thenReturn(time);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("전체 예약 목록을 반환한다.")
    @Test
    void getAll_전체_예약_조회_테스트() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        List<Reservation> reservations = List.of(
                new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L),
                new Reservation("박", time, theme, Status.RESERVED, LocalDateTime.now()).withId(2L)
        );
        when(reservationRepository.findAll()).thenReturn(reservations);

        // when
        List<Reservation> result = reservationService.getAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(reservations);
    }

    @DisplayName("예약을 취소하는 경우, repository의 deleteById가 호출된다.")
    @Test
    void cancel_정상_취소() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.deleteById(1L)).thenReturn(true);

        // when
        reservationService.cancel(1L);

        // then
        verify(reservationRepository).deleteById(1L);
    }

    @DisplayName("대기 순번을 포함한 예약 전체 정보를 조회한다.")
    @Test
    void getAllByName_예약_전체_정보_조회_테스트() {
        // given
        ReservationTime time1 = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime time2 = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme1 = new Theme("테마1", "설명", "test-url").withId(1L);
        Theme theme2 = new Theme("테마2", "설명", "test-url").withId(2L);

        Reservation myReserved = new Reservation("라이", time1, theme1, Status.RESERVED,
                LocalDateTime.of(2030, 5, 1, 10, 0)).withId(1L);
        Reservation myWaiting = new Reservation("라이", time2, theme2, Status.WAITING,
                LocalDateTime.of(2030, 5, 2, 10, 0)).withId(2L);
        Reservation otherEarlierWaiting = new Reservation("어셔", time2, theme2, Status.WAITING,
                LocalDateTime.of(2030, 5, 1, 10, 0)).withId(3L);

        when(reservationRepository.findByName("라이")).thenReturn(List.of(myReserved, myWaiting));
        when(reservationRepository.findAllWaitingBy(time1.getId(), theme1.getId())).thenReturn(List.of());
        when(reservationRepository.findAllWaitingBy(time2.getId(), theme2.getId()))
                .thenReturn(List.of(otherEarlierWaiting, myWaiting));

        // when
        List<ReservationWithWaitingOrderResponse> result = reservationService.getAllByName("라이");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(ReservationWithWaitingOrderResponse::waitingOrder)
                .containsExactly(0, 2);
    }

    @DisplayName("예약 대기가 없는 경우, RESERVED인 예약이 정상 취소된다.")
    @Test
    void cancelForUser_정상_취소() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.findAllWaitingBy(1L, 1L)).thenReturn(List.of());

        // when
        reservationService.cancelForUser(1L, "라이");

        // then
        verify(reservationRepository).deleteById(1L);
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @DisplayName("예약 상태가 WAITING인 경우, 승격없이 WAITING인 예약만 정상 취소된다")
    @Test
    void cancelForUser_WAITING_예약_취소_테스트() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.WAITING, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // when
        reservationService.cancelForUser(1L, "라이");

        // then
        verify(reservationRepository).deleteById(1L);
        verify(reservationRepository, never()).findAllWaitingBy(any(), any());
        verify(reservationRepository, never()).update(any(Reservation.class));
    }

    @DisplayName("자신의 예약이 아닌 예약을 취소하려는 경우, ForbiddenRequestException이 발생한다.")
    @Test
    void cancelForUser_자신_예약_아니면_예외() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(1L, "어셔"))
                .isInstanceOf(ForbiddenRequestException.class)
                .hasMessage("예약에 대한 접근 권한이 없습니다.");
    }

    @DisplayName("존재하지 않는 예약을 사용자가 취소하는 경우, ReservationNotFoundException이 발생한다.")
    @Test
    void cancelForUser_존재하지_않는_예약이면_예외() {
        // given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(999L, "name"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("이미 지난 슬롯의 예약을 취소하는 경우, PastReservationException이 발생한다.")
    @Test
    void cancelForUser_지난_슬롯_예약이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation past = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(past));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(1L, "라이"))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("예약을 취소할 때, 예약 대기가 존재하는 경우, 해당 예약을 삭제하고, 가장 오래된 대기를 예약으로 올린다.")
    @Test
    void cancelForUser_예약_취소_대기_존재_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation reserved = new Reservation("라이", time, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        Reservation oldestWaiting = new Reservation("어셔1", time, theme, Status.WAITING,
                LocalDateTime.of(2030, 5, 1, 10, 0)).withId(2L);
        Reservation newerWaiting = new Reservation("어셔2", time, theme, Status.WAITING,
                LocalDateTime.of(2030, 5, 2, 10, 0)).withId(3L);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reserved));
        when(reservationRepository.findAllWaitingBy(1L, 1L))
                .thenReturn(List.of(newerWaiting, oldestWaiting));

        // when
        reservationService.cancelForUser(1L, "라이");

        // then
        verify(reservationRepository).update(argThat(r ->
                r.getId().equals(oldestWaiting.getId()) && r.getStatus() == Status.RESERVED));
        verify(reservationRepository).deleteById(1L);
    }

    @DisplayName("예약 시간을 변경한다")
    @Test
    void update_정상_변경() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);

        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        Reservation updatedReservation = existing.withTime(newTime);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.isDuplicated(1L, newTime)).thenReturn(false);
        when(reservationRepository.update(any(Reservation.class))).thenReturn(updatedReservation);

        // when
        Reservation result = reservationService.update(1L, 2L, "라이");

        // then
        assertThat(result).isSameAs(updatedReservation);
    }

    @DisplayName("존재하지 않는 예약을 변경하는 경우, ReservationNotFoundException이 발생한다.")
    @Test
    void update_존재하지_않는_예약이면_예외() {
        // given
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.update(999L, 1L, "어셔"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("변경하려는 시간 슬롯이 과거인 경우, PastReservationException이 발생한다.")
    @Test
    void update_과거_슬롯으로_변경하면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L, PAST_START, PAST_END);
        Reservation existing = new Reservation("라이", oldTime, null, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);

        // when & then
        assertThatThrownBy(() -> reservationService.update(1L, 2L, "라이"))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("변경을 원하는 기존 예약의 슬롯이 과거인 경우, PastReservationException이 발생한다.")
    @Test
    void update_기존_예약이_과거이면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, PAST_START, PAST_END);
        Reservation existing = new Reservation("라이", oldTime, null, Status.RESERVED, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> reservationService.update(1L, 2L, "라이"))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("변경하려는 슬롯이 이미 차 있는 경우, Waiting 상태로 변경한다.")
    @Test
    void update_중복_슬롯으로_변경하면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("name", "description", "https://img.test/a.png").withId(1L);
        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        Reservation updated = existing.withTime(newTime).withStatus(Status.WAITING);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.isDuplicatedWithName("라이", 1L, newTime)).thenReturn(false);
        when(reservationRepository.findAllWaitingBy(1L, 1L)).thenReturn(List.of());
        when(reservationRepository.isDuplicated(1L, newTime)).thenReturn(true);
        when(reservationRepository.update(any(Reservation.class))).thenReturn(updated);

        // when
        Reservation result = reservationService.update(1L, 2L, "라이");

        // then
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
        assertThat(result.getTime()).isEqualTo(newTime);
    }

    @DisplayName("자기가 RESERVED이고, 같은 슬롯에 대기가 있으면, 변경 시 대기가 자동으로 승격된다.")
    @Test
    void update_예약대기_자동_승격() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, LocalDateTime.now()).withId(1L);
        Reservation oldestWaiting = new Reservation("어셔", oldTime, theme, Status.WAITING,
                LocalDateTime.of(2030, 5, 1, 10, 0)).withId(2L);
        Reservation updated = existing.withTime(newTime);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.isDuplicatedWithName("라이", 1L, newTime)).thenReturn(false);
        when(reservationRepository.findAllWaitingBy(1L, 1L)).thenReturn(List.of(oldestWaiting));
        when(reservationRepository.isDuplicated(1L, newTime)).thenReturn(false);
        when(reservationRepository.update(any(Reservation.class))).thenReturn(updated);

        // when
        reservationService.update(1L, 2L, "라이");

        // then
        verify(reservationRepository).update(argThat(r ->
                r.getId().equals(oldestWaiting.getId()) && r.getStatus() == Status.RESERVED));
        verify(reservationRepository).update(argThat(r ->
                r.getId().equals(existing.getId()) && r.getTime().equals(newTime)));
    }

    @DisplayName("자기가 WAITING 이면 원 슬롯 자동 승격은 일어나지 않는다.")
    @Test
    void update_자기_WAITING이면_자동_대기_승격_패스() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation existing = new Reservation("라이", oldTime, theme, Status.WAITING, LocalDateTime.now()).withId(1L);
        Reservation updated = existing.withStatus(Status.RESERVED);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.isDuplicatedWithName("라이", 1L, newTime)).thenReturn(false);
        when(reservationRepository.isDuplicated(1L, newTime)).thenReturn(false);
        when(reservationRepository.update(any(Reservation.class))).thenReturn(updated);

        // when
        reservationService.update(1L, 2L, "라이");

        // then
        verify(reservationRepository, times(1)).update(any(Reservation.class));
        verify(reservationRepository, never()).findAllWaitingBy(any(), any());
    }
}
