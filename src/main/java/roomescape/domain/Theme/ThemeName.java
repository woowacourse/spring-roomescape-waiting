package roomescape.domain.Theme;

public class ThemeName {

    private final String name;

    public ThemeName(String name) {
        validateName(name);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private void validateName(String name) {
        if (name.isEmpty() || name.length() > 20) {
            throw new IllegalArgumentException(
                    "[ERROR] 테마이름은 1~20자만 가능합니다.",
                    new Throwable("theme_name : " + name)
            );
        }
    }
}
