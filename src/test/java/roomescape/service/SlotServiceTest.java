package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.exception.ThemeNotFoundException;
import roomescape.exception.TimeSlotNotFoundException;
import roomescape.repository.FakeSlotRepository;
import roomescape.repository.FakeThemeRepository;
import roomescape.repository.FakeTimeSlotRepository;

class SlotServiceTest {

    private SlotService slotService;
    private FakeSlotRepository slotRepository;
    private FakeTimeSlotRepository timeSlotRepository;
    private FakeThemeRepository themeRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;
    private final LocalDate futureDate = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        slotRepository = new FakeSlotRepository();
        timeSlotRepository = new FakeTimeSlotRepository();
        themeRepository = new FakeThemeRepository();
        slotService = new SlotService(slotRepository, timeSlotRepository, themeRepository);
        savedTimeSlot = timeSlotRepository.save(TimeSlot.transientOf(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(Theme.transientOf("테마", "설명", "https://url"));
    }

    @Test
    @DisplayName("슬롯이 존재하지 않으면 새로 생성하여 반환한다.")
    void resolveSlot_createsNew() {
        Slot target = Slot.transientOf(futureDate, savedTimeSlot, savedTheme);
        Slot resolved = slotService.resolveSlot(target);
        assertThat(resolved.getId()).isNotNull();
        assertThat(slotRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("슬롯이 이미 존재하면 기존 슬롯을 반환한다.")
    void resolveSlot_returnsExisting() {
        Slot target = Slot.transientOf(futureDate, savedTimeSlot, savedTheme);
        Slot first = slotService.resolveSlot(target);
        Slot second = slotService.resolveSlot(Slot.transientOf(futureDate, savedTimeSlot, savedTheme));
        assertThat(first.getId()).isEqualTo(second.getId());
        assertThat(slotRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("날짜, 시간 ID, 테마 ID로 슬롯을 조회 또는 생성한다.")
    void resolveNewSlot() {
        Slot slot = slotService.resolveNewSlot(futureDate, savedTimeSlot.getId(), savedTheme.getId());
        assertThat(slot.getDate()).isEqualTo(futureDate);
        assertThat(slot.getTimeSlot().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("조건에 맞는 슬롯이 존재하면 해당 슬롯을 반환한다.")
    void findSlotOrNull_found() {
        slotService.resolveNewSlot(futureDate, savedTimeSlot.getId(), savedTheme.getId());
        Slot found = slotService.findSlotOrNull(futureDate, savedTimeSlot.getId(), savedTheme.getId());
        assertThat(found).isNotNull();
        assertThat(found.getDate()).isEqualTo(futureDate);
    }

    @Test
    @DisplayName("조건에 맞는 슬롯이 없으면 null을 반환한다.")
    void findSlotOrNull_notFound() {
        Slot result = slotService.findSlotOrNull(futureDate, savedTimeSlot.getId(), savedTheme.getId());
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("식별자로 슬롯을 삭제하면 목록에서 사라진다.")
    void deleteSlot() {
        Slot slot = slotService.resolveNewSlot(futureDate, savedTimeSlot.getId(), savedTheme.getId());
        slotService.deleteSlot(slot.getId());
        assertThat(slotRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("null ID를 전달하면 findTimeSlotOrNull은 null을 반환한다.")
    void findTimeSlotOrNull_nullId() {
        TimeSlot result = slotService.findTimeSlotOrNull(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("유효한 ID로 예약 시간을 조회한다.")
    void findTimeSlotOrNull_found() {
        TimeSlot result = slotService.findTimeSlotOrNull(savedTimeSlot.getId());
        assertThat(result).isNotNull();
        assertThat(result.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("존재하지 않는 시간 ID 조회 시 예외가 발생한다.")
    void findTimeSlotOrNull_notFound() {
        assertThatThrownBy(() -> slotService.findTimeSlotOrNull(999L))
                .isInstanceOf(TimeSlotNotFoundException.class);
    }

    @Test
    @DisplayName("null ID를 전달하면 findThemeOrNull은 null을 반환한다.")
    void findThemeOrNull_nullId() {
        Theme result = slotService.findThemeOrNull(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("유효한 ID로 테마를 조회한다.")
    void findThemeOrNull_found() {
        Theme result = slotService.findThemeOrNull(savedTheme.getId());
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테마");
    }

    @Test
    @DisplayName("존재하지 않는 테마 ID 조회 시 예외가 발생한다.")
    void findThemeOrNull_notFound() {
        assertThatThrownBy(() -> slotService.findThemeOrNull(999L))
                .isInstanceOf(ThemeNotFoundException.class);
    }
}
