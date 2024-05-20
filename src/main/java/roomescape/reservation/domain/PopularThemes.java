package roomescape.reservation.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PopularThemes {

    private final List<Theme> populars;

    public PopularThemes(List<Theme> themes) {
        this.populars = makePopularThemes(themes);
    }

    private List<Theme> makePopularThemes(List<Theme> themes) {
        Map<Theme, Integer> countTheme = new HashMap<>();
        for (Theme theme : themes) {
            countTheme.put(theme, countTheme.getOrDefault(theme, 0) + 1);
        }
        return sortThemes(countTheme).stream()
                .limit(10)
                .toList();
    }

    private List<Theme> sortThemes(Map<Theme, Integer> countTheme) {
        List<Entry<Theme, Integer>> list = new ArrayList<>(countTheme.entrySet());
        list.sort(Entry.comparingByValue(Comparator.reverseOrder()));

        return list.stream()
                .map(Entry::getKey)
                .toList();
    }

    public List<Theme> getPopularThemes() {
        return populars;
    }
}
