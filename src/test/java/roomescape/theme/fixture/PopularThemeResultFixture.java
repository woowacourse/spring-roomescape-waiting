package roomescape.theme.fixture;

import roomescape.theme.repository.projection.PopularThemeResult;

public class PopularThemeResultFixture {

    public static PopularThemeResult popularThemeResult(Long id, String name, long reservationCount) {
        return new PopularThemeResult(id, name, "", "", true, reservationCount);
    }

}
