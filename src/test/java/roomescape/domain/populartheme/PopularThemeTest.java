package roomescape.domain.populartheme;

import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class PopularThemeTest {

    @Test
    void 인기_테마_생성_성공_테스트() {
        // given
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        // when
        PopularTheme result = new PopularTheme(theme, 3L);

        // then
        assertAll(
                () -> assertThat(result.getTheme()).isEqualTo(theme),
                () -> assertThat(result.getReservationCount()).isEqualTo(3L));
    }

    @Test
    void 테마가_null이면_예외() {
        // when & then
        assertThatThrownBy(() -> new PopularTheme(null, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("theme은 비어 있을 수 없습니다.");
    }

    @Test
    void 예약수가_null이면_예외() {
        // given
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new PopularTheme(theme, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reservationCount는 비어 있거나 음수일 수 없습니다.");
    }

    @Test
    void 예약수가_음수이면_예외() {
        // given
        Theme theme = new Theme(1L, "테마", "설명", "썸네일");

        // when & then
        assertThatThrownBy(() -> new PopularTheme(theme, -1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reservationCount는 비어 있거나 음수일 수 없습니다.");
    }
}
