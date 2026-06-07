package roomescape.domain.theme;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThemeRanking {

    private final List<Long> themeIds;

    private ThemeRanking(List<Long> themeIds) {
        this.themeIds = themeIds;
    }

    public static ThemeRanking from(List<Long> themeIds) {
        return new ThemeRanking(themeIds);
    }

    public List<Long> topThemeIds(int limit) {
        return themeIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}
