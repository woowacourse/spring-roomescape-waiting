package roomescape.fixture;

import roomescape.theme.domain.Theme;

public class ThemeFixture {

    public static final Long ID = 2L;
    public static final String NAME = "링";
    public static final String DESCRIPTION = "공포 테마";
    public static final String THUMBNAIL_URL = "http://thumbnail.url";
    public static final Theme SAVED = Theme.of(ID, NAME, DESCRIPTION, THUMBNAIL_URL);

    public static Theme savedWith(final Long id, final String name) {
        return Theme.of(id, name, DESCRIPTION, THUMBNAIL_URL);
    }
}
