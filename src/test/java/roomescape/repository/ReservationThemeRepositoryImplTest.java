package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import roomescape.config.JpaConfig;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationThemeRepository;
import roomescape.repository.impl.ReservationThemeRepositoryImpl;
import roomescape.repository.jpa.ReservationThemeJpaRepository;

@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Import(JpaConfig.class)
@DataJpaTest
class ReservationThemeRepositoryImplTest {

    private ReservationThemeRepository repository;

    @Autowired
    private ReservationThemeJpaRepository reservationThemeJpaRepository;

    private ReservationTheme savedTheme;

    @BeforeEach
    void setUp() {
        repository = new ReservationThemeRepositoryImpl(reservationThemeJpaRepository);

        ReservationTheme theme = new ReservationTheme("Theme 1", "Description", "Thumbnail");
        savedTheme = repository.save(theme);
    }

    @DisplayName("id로 테마 데이터를 성공적으로 가져온다.")
    @Test
    void findById() {
        //when
        final Optional<ReservationTheme> theme = repository.findById(savedTheme.getId());

        //then
        assertAll(
                () -> assertThat(theme).isPresent(),
                () -> assertThat(theme.get().getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(theme.get().getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(theme.get().getDescription()).isEqualTo(savedTheme.getDescription()),
                () -> assertThat(theme.get().getThumbnail()).isEqualTo(savedTheme.getThumbnail())
        );
    }

    @DisplayName("모든 테마 데이터를 성공적으로 가져온다.")
    @Test
    void findAll() {
        //when
        final List<ReservationTheme> themes = repository.findAll();

        //then
        assertAll(
                () -> assertThat(themes).isNotEmpty(),
                () -> assertThat(themes).hasSize(1),
                () -> assertThat(themes.get(0).getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(themes.get(0).getName()).isEqualTo(savedTheme.getName())
        );
    }

    @Disabled // TODO: findWeeklyThemeOrderByCountDesc를 파라미터를 넣을 수 있게 만든 뒤 테스트
    @DisplayName("주간 인기테마를 성공적으로 가져온다.")
    @Test
    void findWeeklyThemeOrderByCountDesc() {
        //when
        final List<ReservationTheme> weeklyThemeOrderByCountDesc = repository.findWeeklyThemeOrderByCountDesc();

        //then
        assertThat(weeklyThemeOrderByCountDesc).hasSizeLessThanOrEqualTo(10);
    }

    @DisplayName("테마를 성공적으로 저장한다.")
    @Test
    void save() {
        //given
        final ReservationTheme newTheme = new ReservationTheme("new Theme", "new Description", "new Thumbnail");

        //when
        final ReservationTheme saved = repository.save(newTheme);

        //then
        assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getName()).isEqualTo(newTheme.getName()),
                () -> assertThat(saved.getDescription()).isEqualTo(newTheme.getDescription()),
                () -> assertThat(saved.getThumbnail()).isEqualTo(newTheme.getThumbnail())
        );
    }

    @DisplayName("id로 테마를 성공적으로 삭제한다.")
    @Test
    void deleteById() {
        //given
        Long themeId = savedTheme.getId();

        //when & then
        assertAll(
                () -> assertThatCode(() -> repository.deleteById(themeId)).doesNotThrowAnyException(),
                () -> assertThat(repository.findById(themeId)).isEmpty()
        );
    }

    @DisplayName("이미 존재하는 테마이므로 true를 반환한다.")
    @Test
    void existsByName() {
        //given
        String themeName = savedTheme.getName();

        //when
        final boolean expected = repository.existsByName(themeName);

        //then
        assertThat(expected).isTrue();
    }
}
