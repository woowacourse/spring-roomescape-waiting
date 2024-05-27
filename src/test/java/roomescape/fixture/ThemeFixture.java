package roomescape.fixture;

import roomescape.reservation.domain.Theme;

public class ThemeFixture {
    public static Theme getTheme1() {
        return new Theme(1L, "추리", "추리 테마입니다.", "https://image.yes24.com/goods/73161943/L");
    }

    public static Theme getTheme2() {
        return new Theme(2L, "아날로그식", "아날로그식 테마입니다.", "https://image.yes24.com/goods/62087889/L");
    }
}
