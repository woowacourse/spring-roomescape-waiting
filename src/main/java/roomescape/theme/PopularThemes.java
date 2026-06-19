package roomescape.theme;

import java.util.Comparator;
import java.util.List;
import roomescape.common.DomainAssert;

/**
 * 기간 내 (테마, 예약 수) 목록을 들고, 예약 수 내림차순 상위 N개 테마를 골라낸다.
 * 정렬·상한 같은 랭킹 규칙을 SQL이 아니라 도메인이 책임진다.
 */
public class PopularThemes {
    private final List<ThemeReservationCount> counts;

    public PopularThemes(List<ThemeReservationCount> counts) {
        DomainAssert.notNull(counts, "테마 예약 수 목록은 비어 있을 수 없습니다.");
        this.counts = counts;
    }

    public List<Theme> topN(int limit) {
        return counts.stream()
                .sorted(Comparator.comparingLong(ThemeReservationCount::count).reversed())
                .limit(limit)
                .map(ThemeReservationCount::theme)
                .toList();
    }
}
