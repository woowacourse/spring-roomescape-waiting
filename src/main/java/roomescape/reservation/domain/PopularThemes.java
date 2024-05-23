package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PopularThemes {

    private static final int FIRST_THEME_INDEX = 1;

    private final List<Theme> themes;

    public PopularThemes(List<Theme> themes) {
        this.themes = List.copyOf(themes);
    }

    public List<Theme> findPopularThemesTopOf(int count) {
        Map<String, Long> themeNames = themes.stream()
                .collect(Collectors.groupingBy(Theme::getName, Collectors.counting()));

        return themeNames.entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .flatMap(entry -> themes.stream()
                        .filter(theme -> theme.getName().equals(entry.getKey()))
                        .limit(FIRST_THEME_INDEX))
                .limit(count)
                .toList();
    }
}
