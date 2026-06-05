package roomescape.domain.reservation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Reservations {

    private final List<Long> themeIds;

    private Reservations(List<Long> themeIds) {
        this.themeIds = themeIds;
    }

    public static Reservations from(List<Long> themeIds) {
        return new Reservations(themeIds);
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
