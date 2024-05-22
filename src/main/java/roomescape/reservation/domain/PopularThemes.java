package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PopularThemes {

    private final List<Theme> themes;

    public PopularThemes(List<Theme> themes) {
        this.themes = List.copyOf(themes);
    }

    public List<Theme> findPopularThemesCountOf(int count) {
        Map<Theme, Integer> popularThemes = new HashMap<>();
        for (Theme theme : themes) {
            popularThemes.put(theme, popularThemes.getOrDefault(theme, 0) + 1);
        }

        return popularThemes.entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(count)
                .map(Entry::getKey)
                .toList();
    }
}
