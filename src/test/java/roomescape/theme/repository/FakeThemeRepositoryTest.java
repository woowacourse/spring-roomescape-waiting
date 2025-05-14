//package roomescape.theme.repository;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import roomescape.reservation.fixture.TestFixture;
//import roomescape.theme.domain.Theme;
//
//class FakeThemeRepositoryTest {
//
//    private ThemeRepository themeRepository;
//
//    @BeforeEach
//    void setUp() {
//        themeRepository = new FakeThemeRepository();
//
//        themeRepository.save(TestFixture.makeTheme(1L));
//    }
//
//    @Test
//    void findAll() {
//        List<Theme> themes = themeRepository.findAll();
//
//        assertThat(themes.size()).isEqualTo(1);
//    }
//
//    @Test
//    void save() {
//        themeRepository.save(Theme.of(2L, "theme", "description", "thumbnail"));
//
//        List<Theme> themes = themeRepository.findAll();
//        assertThat(themes.size()).isEqualTo(2);
//    }
//
//    @Test
//    void deleteById() {
//        themeRepository.deleteById(1L);
//
//        List<Theme> themes = themeRepository.findAll();
//        assertThat(themes.size()).isEqualTo(0);
//    }
//
//    @Test
//    void findById() {
//        Optional<Theme> optionalTheme = themeRepository.findById(1L);
//
//        assertThat(optionalTheme.get().getId()).isEqualTo(1L);
//    }
//
//    @Test
//    void findTop10PopularThemesWithinLastWeek() {
//        themeRepository.save(Theme.of(1L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(2L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(3L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(4L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(5L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(6L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(7L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(8L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(9L, "name", "des", "thumbnail"));
//        themeRepository.save(Theme.of(10L, "name", "des", "thumbnail"));
//
//        List<Theme> themes = themeRepository.findTop10PopularThemesWithinLastWeek(
//                LocalDate.now());
//
//        assertThat(themes.size()).isEqualTo(10);
//    }
//}
