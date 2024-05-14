package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialDataFixture.INITIAL_THEME_COUNT;
import static roomescape.InitialDataFixture.NOT_RESERVATION_THEME;
import static roomescape.InitialDataFixture.THEME_1;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.ValidationException;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ThemeH2RepositoryTest {

    @Autowired
    private ThemeH2Repository themeH2Repository;

    @Test
    @DisplayName("Theme를 저장한다.")
    void save() {
        Theme theme = new Theme(null, new Name("레벨2"), "레벨2 설명", "레벨2 썸네일");

        Theme saved = themeH2Repository.save(theme);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("id에 맞는 Theme을 제거한다.")
    void delete() {
        themeH2Repository.delete(NOT_RESERVATION_THEME.getId());

        assertThat(themeH2Repository.findById(NOT_RESERVATION_THEME.getId()).isEmpty()).isTrue();
    }

    @Test
    @DisplayName("참조되어 있는 테마를 삭제하는 경우 예외가 발생한다.")
    void deleteReferencedTime() {
        assertThatThrownBy(() -> themeH2Repository.delete(THEME_1.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("모든 theme를 찾는다.")
    void findAll() {
        List<Theme> found = themeH2Repository.findAll();

        assertThat(found).hasSize(INITIAL_THEME_COUNT);
    }

    @Test
    @DisplayName("id에 맞는 theme를 찾는다.")
    void findBy() {
        Theme found = themeH2Repository.findById(THEME_1.getId()).get();

        assertThat(found.getName()).isEqualTo(THEME_1.getName());
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void findEmpty() {
        Optional<Theme> theme = themeH2Repository.findById(-1L);

        assertThat(theme.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("이미 예약된 정보를 바탕으로 인기테마를 찾는다.")
    void findTrendings() {
        List<Theme> trendings = themeH2Repository.findTrendings(
                LocalDate.parse("2024-04-25"),
                LocalDate.parse("2024-04-26"),
                1L
        );

        assertThat(trendings).containsExactly(THEME_1);
    }
}
