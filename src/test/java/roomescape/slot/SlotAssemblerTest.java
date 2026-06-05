package roomescape.slot.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.EscapeRoomException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.infrastructure.ReservationTimeRepository;
import roomescape.slot.application.SlotAssembler;
import roomescape.slot.application.port.out.SlotRepository;
import roomescape.theme.Theme;
import roomescape.theme.infrastructure.ThemeRepository;

@ExtendWith(MockitoExtension.class)
class SlotAssemblerTest {

    @Mock
    private SlotRepository slotRepository;

    @Mock
    private ReservationTimeRepository reservationTimeRepository;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private SlotAssembler slotAssembler;

    @Test
    @DisplayName("조건에 맞는 기존 슬롯을 도메인으로 조립한다.")
    void assembleExisting_success() {
        LocalDate date = LocalDate.of(2026, 5, 7);
        long timeId = 1L;
        long themeId = 2L;
        long slotId = 10L;
        Slot expectedSlot = Slot.of(
                slotId,
                date,
                new ReservationTime(timeId, LocalTime.of(10, 0)),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
        givenNow(LocalDate.of(2026, 5, 6));

        when(slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId))
                .thenReturn(Optional.of(expectedSlot));

        Slot slot = slotAssembler.assembleExisting(date, timeId, themeId);

        assertThat(slot.getId()).isEqualTo(slotId);
        assertThat(slot.getDate()).isEqualTo(date);
        assertThat(slot.getTimeId()).isEqualTo(timeId);
        assertThat(slot.getThemeId()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("새 슬롯을 도메인으로 조립한다.")
    void assembleNew_success() {
        LocalDate date = LocalDate.of(2026, 5, 7);
        long timeId = 1L;
        long themeId = 2L;
        givenNow(LocalDate.of(2026, 5, 6));

        when(reservationTimeRepository.findById(timeId))
                .thenReturn(Optional.of(new ReservationTime(timeId, LocalTime.of(10, 0))));
        when(themeRepository.findById(themeId))
                .thenReturn(Optional.of(new Theme(themeId, "theme", "description", "thumbnail")));

        Slot slot = slotAssembler.assembleNew(date, timeId, themeId);

        assertThat(slot.getId()).isNull();
        assertThat(slot.getDate()).isEqualTo(date);
        assertThat(slot.getTimeId()).isEqualTo(timeId);
        assertThat(slot.getThemeId()).isEqualTo(themeId);
    }

    @Test
    @DisplayName("조립한 슬롯이 과거이면 예외가 발생한다.")
    void assembleExisting_past_fail() {
        LocalDate date = LocalDate.of(2026, 5, 5);
        long timeId = 1L;
        long themeId = 2L;
        Slot pastSlot = Slot.of(
                10L,
                date,
                new ReservationTime(timeId, LocalTime.of(10, 0)),
                new Theme(themeId, "theme", "description", "thumbnail")
        );
        givenNow(LocalDate.of(2026, 5, 6));

        when(slotRepository.findByDateAndTimeIdAndThemeId(date, timeId, themeId))
                .thenReturn(Optional.of(pastSlot));

        assertThatThrownBy(() -> slotAssembler.assembleExisting(date, timeId, themeId))
                .isInstanceOf(EscapeRoomException.class);
    }

    private void givenNow(LocalDate date) {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(
                date.atStartOfDay(ZoneId.systemDefault()).toInstant()
        );
    }
}
