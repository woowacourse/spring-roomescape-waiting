package roomescape.application.reservation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@DisplayName("예약 슬롯 배치 생성기")
@ExtendWith(MockitoExtension.class)
class ReservationSlotBatchGeneratorTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2030-01-01T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private ReservationSlotRepository slotRepository;

    @Mock
    private ReservationTimeRepository timeRepository;

    @Mock
    private ThemeRepository themeRepository;

    private ReservationSlotBatchGenerator batchGenerator;

    @BeforeEach
    void setUp() {
        batchGenerator = new ReservationSlotBatchGenerator(
                slotRepository,
                timeRepository,
                themeRepository,
                FIXED_CLOCK
        );
    }

    @DisplayName("특정 날짜의 모든 테마와 시간에 대해 존재하지 않는 슬롯을 생성할 수 있다")
    @Test
    void generateSlotsForDate() {
        // given
        LocalDate date = LocalDate.of(2030, 1, 15);
        Theme theme = Theme.of(1L, "공포", "오금이 저리는 테마", "/themes/scary");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));

        given(themeRepository.findAll()).willReturn(List.of(theme));
        given(timeRepository.findAll()).willReturn(List.of(time));
        given(slotRepository.existsByDateAndThemeIdAndTimeId(date, theme.getId(), time.getId())).willReturn(false);

        // when
        batchGenerator.generateSlotsForDate(date);

        // then
        verify(slotRepository, times(1)).save(any(ReservationSlot.class));
    }

    @DisplayName("이미 존재하는 슬롯은 생성하지 않고 건너뛴다")
    @Test
    void generateSlotsForDateWhenAlreadyExists() {
        // given
        LocalDate date = LocalDate.of(2030, 1, 15);
        Theme theme = Theme.of(1L, "공포", "오금이 저리는 테마", "/themes/scary");
        ReservationTime time = ReservationTime.of(1L, LocalTime.of(10, 0));

        given(themeRepository.findAll()).willReturn(List.of(theme));
        given(timeRepository.findAll()).willReturn(List.of(time));
        given(slotRepository.existsByDateAndThemeIdAndTimeId(date, theme.getId(), time.getId())).willReturn(true);

        // when
        batchGenerator.generateSlotsForDate(date);

        // then
        verify(slotRepository, never()).save(any(ReservationSlot.class));
    }
}
