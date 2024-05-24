package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PopularThemes {

    private static final int POPULAR_THEME_SIZE = 10;
    private final List<Theme> populars;

    public PopularThemes(List<Theme> themes) {
        this.populars = makePopularThemes(themes);
    }

    private List<Theme> makePopularThemes(List<Theme> themes) {
        Map<String, Long> countTheme = themes.stream()
                .collect(Collectors.groupingBy(Theme::getName, Collectors.counting()));

        return countTheme.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(POPULAR_THEME_SIZE)
                .flatMap(entry -> themes.stream()
                        .filter(theme -> theme.getName().equals(entry.getKey()))
                        .findFirst()
                        .stream())
                .toList();
    }

    public List<Theme> getPopularThemes() {
        return populars;
    }
}
