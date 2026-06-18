package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.holiday.service.HolidayService;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.controller.dto.ReservationWithWaitingOrderResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.exception.DuplicateReservationException;
import roomescape.reservation.exception.ForbiddenRequestException;
import roomescape.reservation.exception.PastReservationException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.reservation.service.dto.ReservationSaveServiceRequest;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeNotFoundException;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.exception.TimeNotFoundException;
import roomescape.time.service.TimeService;

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

    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationServiceImpl(
                reservationRepository, timeService, themeRepository, holidayService);
    }

    @DisplayName("정상 입력이고 대기가 없는 경우, 저장된 예약을 반환한다.")
    @Test
    void create_정상_예약_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.existsById(1L)).thenReturn(true);
        when(holidayService.isHoliday(any())).thenReturn(false);
        when(reservationRepository.hasConfirmedReservation(any(), any())).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);
        when(themeRepository.findById(1L)).thenReturn(theme);

        // when
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L, null, null);
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
        Reservation saved = new Reservation("라이", time, theme, Status.WAITING, null, null, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.existsById(1L)).thenReturn(true);
        when(holidayService.isHoliday(any())).thenReturn(false);
        when(reservationRepository.hasConfirmedReservation(any(), any())).thenReturn(true);
        when(reservationRepository.save(any())).thenReturn(saved);
        when(themeRepository.findById(1L)).thenReturn(theme);

        // when
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L, null, null);
        Reservation result = reservationService.create(dto);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("라이");
        assertThat(result.getStatus()).isEqualTo(Status.WAITING);
    }

    @DisplayName("timeId가 null인 경우, IllegalArgumentException이 발생한다.")
    @Test
    void create_timeId가_null이면_예외() {
        // given
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, null, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간은 필수입니다.");
    }

    @DisplayName("존재하지 않는 timeId인 경우, TimeNotFoundException이 발생한다.")
    @Test
    void create_존재하지_않는_timeId이면_예외() {
        // given
        when(timeService.findById(999L)).thenThrow(new TimeNotFoundException(999L));
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 999L, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(TimeNotFoundException.class);
    }

    @DisplayName("themeId가 null인 경우, IllegalArgumentException이 발생한다.")
    @Test
    void create_themeId가_null이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        when(timeService.findById(1L)).thenReturn(time);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", null, 1L, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @DisplayName("존재하지 않는 themeId인 경우, ThemeNotFoundException이 발생한다.")
    @Test
    void create_존재하지_않는_themeId이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.existsById(999L)).thenReturn(false);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 999L, 1L, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @DisplayName("휴일에 예약을 시도하는 경우, IllegalArgumentException이 발생한다.")
    @Test
    void create_휴일이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.existsById(1L)).thenReturn(true);
        when(holidayService.isHoliday(FUTURE_START.toLocalDate())).thenReturn(true);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("휴일은 예약이 불가합니다.");
    }

    @DisplayName("같은 슬롯/테마에 중복 예약을 시도하는 경우, 대기로 넘어가기 때문에 예외가 발생하지 않는다.")
    @Test
    void create_중복_예약이면_대기로_저장() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation saved = new Reservation("라이", time, theme, Status.WAITING, null, null, LocalDateTime.now()).withId(1L);

        when(timeService.findById(1L)).thenReturn(time);
        when(themeRepository.existsById(1L)).thenReturn(true);
        when(holidayService.isHoliday(FUTURE_START.toLocalDate())).thenReturn(false);
        when(reservationRepository.hasConfirmedReservation(any(), any())).thenReturn(true);
        when(themeRepository.findById(1L)).thenReturn(theme);
        when(reservationRepository.isDuplicatedWithName(any(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any())).thenReturn(saved);
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L, null, null);

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
        ReservationSaveServiceRequest dto = new ReservationSaveServiceRequest("라이", 1L, 1L, null, null);

        // when & then
        assertThatThrownBy(() -> reservationService.create(dto))
                .isInstanceOf(PastReservationException.class)
                .hasMessage("과거 날짜·시간은 예약이 불가합니다.");
    }

    @DisplayName("전체 예약 목록을 반환한다.")
    @Test
    void getAll_전체_예약_조회_테스트() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        List<Reservation> reservations = List.of(
                new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L),
                new Reservation("박", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(2L)
        );
        when(reservationRepository.findAll()).thenReturn(reservations);

        // when
        List<Reservation> result = reservationService.getAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(reservations);
    }

    @DisplayName("대기 순번을 포함한 예약 전체 정보를 조회한다.")
    @Test
    void getAllByName_예약_전체_정보_조회_테스트() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Theme theme2 = new Theme("테마", "설명", "https://img.test/a.png").withId(2L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        List<ReservationWithWaitingOrder> reservationWithWaitingOrders = List.of(
                new ReservationWithWaitingOrder(1L, "라이", ReservationTimeResponse.from(time),
                        ThemeResponse.from(theme), Status.RESERVED, null),
                new ReservationWithWaitingOrder(1L, "라이", ReservationTimeResponse.from(time),
                        ThemeResponse.from(theme2), Status.WAITING, 3)
        );
        when(reservationRepository.findAllByName("라이")).thenReturn(reservationWithWaitingOrders);

        // when
        List<ReservationWithWaitingOrderResponse> result = reservationService.getAllByName("라이");

        // then
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().waitingOrder()).isNull();
        assertThat(result.get(1).waitingOrder()).isEqualTo(3);
        verify(reservationRepository, times(1)).findAllByName("라이");
    }

    @DisplayName("예약을 취소하는 경우, repository의 deleteById가 호출된다.")
    @Test
    void cancel_정상_취소() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.findEarliestWaiting(any(), any())).thenReturn(Optional.empty());

        // when
        reservationService.cancel(1L);

        // then
        verify(reservationRepository).deleteById(1L);
    }

    @DisplayName("예약 대기가 없는 경우, RESERVED인 예약이 정상 취소된다.")
    @Test
    void cancelForUser_정상_취소() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(reservation.getId())).thenReturn(Optional.of(reservation));
        when(reservationRepository.findEarliestWaiting(any(), any())).thenReturn(Optional.empty());

        // when
        reservationService.cancelForUser(reservation.getId(), "라이");

        // then
        verify(reservationRepository).deleteById(reservation.getId());
        verify(reservationRepository, never()).promoteToReserved(any());
    }

    @DisplayName("자신의 예약이 아닌 예약을 취소하려는 경우, ForbiddenRequestException이 발생한다.")
    @Test
    void cancelForUser_자신_예약_아니면_예외() {
        // given
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(reservation.getId(), "어셔"))
                .isInstanceOf(ForbiddenRequestException.class)
                .hasMessage("예약에 대한 접근 권한이 없습니다.");
    }

    @DisplayName("존재하지 않는 예약을 사용자가 취소하는 경우, ReservationNotFoundException이 발생한다.")
    @Test
    void cancelForUser_존재하지_않는_예약이면_예외() {
        // given
        when(reservationRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(999L, "name"))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("이미 지난 슬롯의 예약을 취소하는 경우, PastReservationException이 발생한다.")
    @Test
    void cancelForUser_지난_슬롯_예약이면_예외() {
        // given
        ReservationTime time = new ReservationTime(1L, PAST_START, PAST_END);
        Reservation past = new Reservation("라이", time, null, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(past.getId())).thenReturn(Optional.of(past));

        // when & then
        assertThatThrownBy(() -> reservationService.cancelForUser(past.getId(), "라이"))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("예약을 취소할 때, 예약 대기가 존재하는 경우, 해당 예약을 삭제하고, 가장 오래된 대기를 예약으로 올린다.")
    @Test
    void cancelForUser_예약_취소_대기_존재_테스트() {
        // given
        ReservationTime time = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation reservation = new Reservation("라이", time, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.findEarliestWaiting(time.getId(), theme.getId())).thenReturn(Optional.of(2L));
        when(reservationRepository.promoteToReserved(2L)).thenReturn(true);
        when(reservationRepository.deleteById(reservation.getId())).thenReturn(true);

        // when
        reservationService.cancelForUser(reservation.getId(), "라이");

        // then
        verify(reservationRepository).findByIdForUpdate(1L);
        verify(reservationRepository).findEarliestWaiting(time.getId(), theme.getId());
        verify(reservationRepository).promoteToReserved(2L);
        verify(reservationRepository).deleteById(reservation.getId());
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
        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(existing.getId())).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.hasConfirmedReservation(theme.getId(), newTime)).thenReturn(false);
        when(reservationRepository.update(any(), any(), any(), any())).thenReturn(true);

        // when
        Reservation result = reservationService.update(existing.getId(), newTime.getId());

        // then
        assertThat(result.getTime().getId()).isEqualTo(2L);
        verify(reservationRepository).update(any(), any(), any(), any());
    }

    @DisplayName("존재하지 않는 예약을 변경하는 경우, ReservationNotFoundException이 발생한다.")
    @Test
    void update_존재하지_않는_예약이면_예외() {
        // given
        when(reservationRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reservationService.update(999L, 1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @DisplayName("변경하려는 시간 슬롯이 과거인 경우, PastReservationException이 발생한다.")
    @Test
    void update_과거_슬롯으로_변경하면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L, PAST_START, PAST_END);
        Theme theme = new Theme("테마1", "설명", "https://img.test/a.png").withId(1L);
        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(existing.getId())).thenReturn(Optional.of(existing));
        when(timeService.findById(newTime.getId())).thenReturn(newTime);

        // when & then
        assertThatThrownBy(() -> reservationService.update(existing.getId(), newTime.getId()))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("변경을 원하는 기존 예약의 슬롯이 과거인 경우, PastReservationException이 발생한다.")
    @Test
    void update_기존_예약이_과거이면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, PAST_START, PAST_END);
        Reservation existing = new Reservation("라이", oldTime, null, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(existing.getId())).thenReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> reservationService.update(existing.getId(), oldTime.getId()))
                .isInstanceOf(PastReservationException.class);
    }

    @DisplayName("변경하려는 슬롯에 같은 이름의 예약/대기가 이미 있는 경우, DuplicateReservationException이 발생한다.")
    @Test
    void update_중복_슬롯으로_변경하면_예외() {
        // given
        ReservationTime oldTime = new ReservationTime(1L, FUTURE_START, FUTURE_END);
        ReservationTime newTime = new ReservationTime(2L,
                LocalDateTime.of(2030, 6, 1, 14, 0),
                LocalDateTime.of(2030, 6, 1, 16, 0));
        Theme theme = new Theme("테마", "설명", "https://img.test/a.png").withId(1L);
        Reservation existing = new Reservation("라이", oldTime, theme, Status.RESERVED, null, null, LocalDateTime.now()).withId(1L);
        when(reservationRepository.findByIdForUpdate(existing.getId())).thenReturn(Optional.of(existing));
        when(timeService.findById(2L)).thenReturn(newTime);
        when(reservationRepository.isDuplicatedWithName("라이", 1L, newTime)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reservationService.update(existing.getId(), newTime.getId()))
                .isInstanceOf(DuplicateReservationException.class);
    }
}
