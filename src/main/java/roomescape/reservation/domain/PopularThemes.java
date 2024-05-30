package roomescape.reservation.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PopularThemes {

    private final List<Theme> populars;

    public PopularThemes(List<Theme> themes) {
        this.populars = makePopularThemes(themes);
    }

    private List<Theme> makePopularThemes(List<Theme> themes) {
        return themes.stream()
                .collect(Collectors.groupingBy(theme -> theme, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Theme, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<Theme> getPopularThemes() {
        return populars;
    }
}
