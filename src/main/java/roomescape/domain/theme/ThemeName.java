package roomescape.domain.theme;

import jakarta.persistence.Embeddable;

@Embeddable
public class ThemeName {

    private static final int MAX_THEME_NAME_LENGTH = 20;

    private String themeName;

    protected ThemeName() {
    }

    public ThemeName(String themeName) {
        validateName(themeName);
        this.themeName = themeName;
    }

    public String getThemeName() {
        return themeName;
    }

    private void validateName(String name) {
        if (name.isEmpty() || name.length() > MAX_THEME_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "[ERROR] 테마이름은 1~20자만 가능합니다.",
                    new Throwable("theme_name : " + name)
            );
        }
    }
}
