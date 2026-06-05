package roomescape.slot.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.slot.application.SlotAssembler;
import roomescape.slot.application.SlotService;
import roomescape.slot.application.dto.request.SlotSaveRequest;
import roomescape.slot.application.dto.response.SlotFindResponse;
import roomescape.slot.application.dto.response.SlotSaveResponse;
import roomescape.slot.application.port.out.SlotRepository;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlotServiceTest {

    @Mock
    private SlotRepository slotRepository;
    @Mock
    private SlotAssembler slotAssembler;

    @InjectMocks
    private SlotService slotService;

    @Test
    @DisplayName("슬롯 저장에 성공한다.")
    void save_성공_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 10), 1L, 2L);
        ReservationTime reservationTime = new ReservationTime(request.timeId(), LocalTime.of(10, 0));
        Theme theme = new Theme(request.themeId(), "test", "testDescription", "testUrl");
        Slot slot = Slot.create(request.date(), reservationTime, theme);
        Slot savedSlot = Slot.of(10L, LocalDate.of(2026, 5, 10), reservationTime, theme);
        when(slotAssembler.assembleNew(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot);
        when(slotRepository.save(slot)).thenReturn(savedSlot);

        SlotSaveResponse response = slotService.save(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.date()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(response.time_id()).isEqualTo(1L);
        assertThat(response.theme_id()).isEqualTo(2L);
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
    @DisplayName("슬롯 저장 시 중복 슬롯이면 예외가 발생한다.")
    void save_중복_슬롯_실패_테스트() {
        SlotSaveRequest request = new SlotSaveRequest(LocalDate.of(2026, 5, 10), 1L, 2L);
        ReservationTime reservationTime = new ReservationTime(request.timeId(), LocalTime.of(10, 0));
        Theme theme = new Theme(request.themeId(), "test", "testDescription", "testUrl");
        Slot slot = Slot.create(request.date(), reservationTime, theme);

        when(slotAssembler.assembleNew(request.date(), request.timeId(), request.themeId()))
                .thenReturn(slot);
        when(slotRepository.existsByDateAndThemeIdAndTimeId(request.date(), request.themeId(), request.timeId()))
                .thenReturn(true);

        assertThatThrownBy(() -> slotService.save(request))
                .isInstanceOf(roomescape.exception.EscapeRoomException.class);

        verify(slotRepository, never()).save(any(Slot.class));
    }
}
