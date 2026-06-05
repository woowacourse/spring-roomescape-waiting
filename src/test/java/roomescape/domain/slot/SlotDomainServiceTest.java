package roomescape.domain.slot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.exception.ExpiredDateTimeException;
import roomescape.exception.ReservationTimeNotFoundException;
import roomescape.exception.ThemeNotFoundException;
import roomescape.fake.FakeReservationTimeRepository;
import roomescape.fake.FakeSlotRepository;
import roomescape.fake.FakeThemeRepository;

class SlotDomainServiceTest {

    private static final Long TIME_ID = 1L;
    private static final Long THEME_ID = 2L;
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    private FakeSlotRepository slotRepository;
    private SlotDomainService slotDomainService;

    @BeforeEach
    void setUp() {
        slotRepository = new FakeSlotRepository();
        FakeReservationTimeRepository timeRepository = new FakeReservationTimeRepository();
        FakeThemeRepository themeRepository = new FakeThemeRepository();
        slotDomainService = new SlotDomainService(slotRepository, timeRepository, themeRepository);

        timeRepository.save(new ReservationTime(TIME_ID, LocalTime.of(10, 0)));
        themeRepository.save(new Theme(THEME_ID, "테마", "설명", "url"));
    }

    @Test
    void 슬롯이_없으면_새로_생성한다() {
        Slot created = slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDate()).isEqualTo(TOMORROW);
        assertThat(created.getTime().getId()).isEqualTo(TIME_ID);
        assertThat(created.getTheme().getId()).isEqualTo(THEME_ID);
    }

    @Test
    void 이미_존재하는_슬롯이면_기존_슬롯을_반환한다() {
        Slot first = slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);
        Slot second = slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);

        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    void 존재하지_않는_시간으로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> slotDomainService.create(TOMORROW, 999L, THEME_ID))
                .isInstanceOf(ReservationTimeNotFoundException.class);
    }

    @Test
    void 존재하지_않는_테마로_생성하면_예외가_발생한다() {
        assertThatThrownBy(() -> slotDomainService.create(TOMORROW, TIME_ID, 999L))
                .isInstanceOf(ThemeNotFoundException.class);
    }

    @Test
    void 과거_날짜로_생성하면_예외가_발생한다() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> slotDomainService.create(yesterday, TIME_ID, THEME_ID))
                .isInstanceOf(ExpiredDateTimeException.class);
    }

    @Test
    void find는_슬롯이_없으면_빈_값을_반환한다() {
        Optional<Slot> found = slotDomainService.find(TOMORROW, TIME_ID, THEME_ID);

        assertThat(found).isEmpty();
    }

    @Test
    void find는_슬롯이_있으면_반환한다() {
        slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);

        Optional<Slot> found = slotDomainService.find(TOMORROW, TIME_ID, THEME_ID);

        assertThat(found).isPresent();
    }

    @Test
    void isExistByDateAndTimeAndTheme는_존재_여부를_반환한다() {
        assertThat(slotDomainService.isExistByDateAndTimeAndTheme(TOMORROW, TIME_ID, THEME_ID)).isFalse();

        slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);

        assertThat(slotDomainService.isExistByDateAndTimeAndTheme(TOMORROW, TIME_ID, THEME_ID)).isTrue();
    }

    @Test
    void delete는_슬롯을_제거한다() {
        Slot created = slotDomainService.create(TOMORROW, TIME_ID, THEME_ID);

        slotDomainService.delete(created.getId());

        assertThat(slotDomainService.find(TOMORROW, TIME_ID, THEME_ID)).isEmpty();
    }
}
