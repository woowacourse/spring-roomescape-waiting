package roomescape.slot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.slot.application.SlotService;
import roomescape.slot.dto.request.SlotSaveRequest;
import roomescape.slot.dto.response.SlotFindResponse;
import roomescape.slot.dto.response.SlotSaveResponse;
import roomescape.slot.infrastructure.SlotRepository;
import roomescape.theme.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private ThemeRepository themeRepository;
    @Mock
    private ReservationTimeRepository reservationTimeRepository;
    @Mock
    private Clock clock;

    @InjectMocks
    private SlotService slotService;

    @Test
    @DisplayName("슬롯 저장에 성공한다.")
    void save_성공_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 10), 1L, 2L);
        ReservationTime reservationTime = new ReservationTime(request.timeId(), LocalTime.of(10, 0));
        Theme theme = new Theme(request.themeId(), "test", "testDescription", "testUrl");
        Slot savedSlot = Slot.of(10L, LocalDate.of(2026, 5, 10), reservationTime, theme);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(reservationTimeRepository.findById(request.timeId()))
                .thenReturn(Optional.of(reservationTime));
        when(themeRepository.findById(request.themeId()))
                .thenReturn(Optional.of(theme));
        when(slotRepository.save(any(Slot.class))).thenReturn(savedSlot);

        SlotSaveResponse response = slotService.save(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(response.time_id()).isEqualTo(1L);
        assertThat(response.theme_id()).isEqualTo(2L);
    }

    @Test
    @DisplayName("슬롯 저장 시 요청으로 들어온 timeId가 시간 목록에 존재하지 않는다면 예외가 발생한다.")
    void save_존재하지_않는_시간_실패_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 10), 999L, 2L);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(reservationTimeRepository.findById(request.timeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.save(request))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("슬롯 저장 시 요청으로 들어온 themeId가 테마 목록에 존재하지 않는다면 예외가 발생한다.")
    void save_존재하지_않는_테마_실패_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 10), 1L, 999L);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        when(reservationTimeRepository.findById(request.timeId()))
                .thenReturn(Optional.of(new ReservationTime(request.timeId(), LocalTime.of(10, 0))));
        when(themeRepository.findById(request.themeId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> slotService.save(request))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("ID로 슬롯 단건 조회에 성공한다.")
    void findById_성공_테스트() {
        Slot slot = Slot.of(
                1L,
                LocalDate.of(2026, 5, 5),
                new ReservationTime(1L, LocalTime.of(10, 0)),
                new Theme(1L, "test", "testDescription", "testUrl")
        );
        when(slotRepository.findById(1L)).thenReturn(Optional.of(slot));

        SlotFindResponse response = slotService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 5));
        assertThat(response.time_id()).isEqualTo(1L);
        assertThat(response.theme_id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("슬롯 삭제를 요청한다.")
    void deleteById_테스트() {
        // when
        slotService.deleteById(1L);

        // then
        verify(slotRepository).deleteById(1L);
    }

    @Test
    @DisplayName("요청으로 들어온 date가 과거 날짜이면 예외가 발생한다.")
    void validateSlot_테스트_1() {
        // given: 오늘을 2026-05-06으로 고정
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        LocalDate beforeDate = LocalDate.of(2026, 5, 5);
        Long testTimeId = 1L;
        Long testThemeId = 1L;

        // when, then
        assertThatThrownBy(() -> slotService.validateSlot(beforeDate, testTimeId, testThemeId))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("요청으로 들어온 timeId가 시간 목록에 존재하지 않는다면 예외가 발생한다.")
    void validateSlot_테스트_2() {
        // given: 오늘을 2026-05-06으로 고정
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        LocalDate date = LocalDate.now(clock);
        Long testTimeId = 999L;
        Long testThemeId = 1L;

        // when, then
        assertThatThrownBy(() -> slotService.validateSlot(date, testTimeId, testThemeId))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("요청으로 들어온 themeId가 테마 목록에 존재하지 않는다면 예외가 발생한다.")
    void validateSlot_테스트_3() {
        // given: 오늘을 2026-05-06으로 고정
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        LocalDate date = LocalDate.now(clock);
        long testTimeId = 1L;
        long testThemeId = 999L;

        when(reservationTimeRepository.findById(testTimeId)).thenReturn(Optional.of(new ReservationTime(1L, LocalTime.of(10, 0))));
        when(themeRepository.findById(testThemeId)).thenReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> slotService.validateSlot(date, testTimeId, testThemeId))
                .isInstanceOf(EscapeRoomException.class);
    }

    @Test
    @DisplayName("요청으로 들어온 body가 모두 정상이면 예외를 반환하지 않는다.")
    void validateSlot_테스트_4() {
        // given: 오늘을 2026-05-06으로 고정
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                LocalDate.of(2026, 5, 6)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );
        LocalDate date = LocalDate.now(clock);
        long testTimeId = 1L;
        long testThemeId = 1L;

        when(reservationTimeRepository.findById(testTimeId)).thenReturn(Optional.of(new ReservationTime(testTimeId, LocalTime.of(10, 0))));
        when(themeRepository.findById(testThemeId)).thenReturn(Optional.of(new Theme(testThemeId, "test", "testDescription", "testUrl")));
        // when, then
        assertThatCode(() -> slotService.validateSlot(date, testTimeId, testThemeId))
                .doesNotThrowAnyException();
        verify(reservationTimeRepository, times(1)).findById(testTimeId);
        verify(themeRepository, times(1)).findById(testThemeId);
    }
}
