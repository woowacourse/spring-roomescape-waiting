package roomescape.reservation.repository.fixture;

import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public enum ThemeFixture {

    THEME1(1L, "링", "이거 겁나 무서움", "링 썸네일"),
    THEME2(2L, "도시괴담", "이건 조금 덜 무서움", "도시괴담 썸네일"),
    THEME3(3L, "콜러", "공포 테마 중독자 추천", "콜러 썸네일"),
    THEME4(4L, "제로", "심약자는 도전하지 마시오", "제로 썸네일"),
    ;

    private final long id;
    private final String themeName;
    private final String description;
    private final String thumbnail;

    ThemeFixture(long id, String themeName, String description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static int count() {
        return values().length;
    }

    public Theme create() {
        return new Theme(
                id, new ThemeName(themeName), description, thumbnail
        );
    }
}
