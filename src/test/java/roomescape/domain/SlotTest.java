package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.vo.Name;

class SlotTest {

    private final LocalDate date = LocalDate.now().plusDays(1);
    private final Time time = new Time(1L, LocalTime.of(13, 0));
    private final Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");

    @Test
    @DisplayName("날짜, 시간, 테마, 매장이 같으면 같은 슬롯이다")
    void sameSlot() {
        Slot slot = new Slot(date, time, theme, 1L);
        Slot same = new Slot(
                date,
                new Time(1L, LocalTime.of(13, 0)),
                new Theme(1L, new Name("다른 이름"), "http://other", "다른 설명"),
                1L);

        assertThat(slot).isEqualTo(same);
    }

    @Test
    @DisplayName("같은 슬롯은 같은 해시 코드를 가진다")
    void sameHashCode() {
        Slot slot = new Slot(date, time, theme, 1L);
        Slot same = new Slot(
                date,
                new Time(1L, LocalTime.of(13, 0)),
                new Theme(1L, new Name("다른 이름"), "http://other", "다른 설명"),
                1L);

        assertThat(slot).hasSameHashCodeAs(same);
    }

    @Test
    @DisplayName("날짜, 시간, 테마, 매장 중 하나라도 다르면 다른 슬롯이다")
    void differentSlot() {
        Slot slot = new Slot(date, time, theme, 1L);

        assertThat(slot).isNotEqualTo(new Slot(date.plusDays(1), time, theme, 1L));
        assertThat(slot).isNotEqualTo(new Slot(date, new Time(2L, LocalTime.of(14, 0)), theme, 1L));
        assertThat(slot).isNotEqualTo(new Slot(
                date, time, new Theme(2L, new Name("테마2"), "http://thumbnail", "설명"), 1L));
        assertThat(slot).isNotEqualTo(new Slot(date, time, theme, 2L));
    }
}
