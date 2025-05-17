package roomescape.domain.theme;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import roomescape.domain.reservation.Reservation;

public class ThemeRanking {

    private static final int RANKING_SIZE = 10;

    private final Map<Theme, Integer> themeRankCount;

    public ThemeRanking(List<Reservation> inRangeReservations) {
        HashMap<Theme, Integer> calculateThemeRankCount = new HashMap<>();
        for (Reservation inRangeReservation : inRangeReservations) {
            Theme theme = inRangeReservation.getTheme();
            calculateThemeRankCount.put(theme, calculateThemeRankCount.getOrDefault(theme, 0) + 1);
        }
        this.themeRankCount = calculateThemeRankCount;
    }

    public List<Theme> getAscendingRanking() {
        return themeRankCount.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(RANKING_SIZE)
                .toList();
    }
}
