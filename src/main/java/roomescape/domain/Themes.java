package roomescape.domain;

import java.util.Collections;
import java.util.List;

public class Themes {
    private final List<Theme> themes;

    public Themes(List<Theme> themes) {
        this.themes = themes;
    }

    public boolean hasNameOf(String name) {
        return themes.stream()
                .anyMatch(theme -> theme.isNameOf(name));
    }

    public List<Theme> getThemes() {
        return Collections.unmodifiableList(themes);
    }
}
