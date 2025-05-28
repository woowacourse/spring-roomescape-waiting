package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    void PK와_함께_생성된다() {

        // given
        Theme themeWithoutId = ThemeFixture.createWithoutId();
        Long primaryKey = 1L;

        Theme expected = new Theme(
            primaryKey,
            themeWithoutId.getName(),
            themeWithoutId.getDescription(),
            themeWithoutId.getThumbnail()
        );

        // when
        Theme actual = Theme.createWithPrimaryKey(themeWithoutId, primaryKey);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
