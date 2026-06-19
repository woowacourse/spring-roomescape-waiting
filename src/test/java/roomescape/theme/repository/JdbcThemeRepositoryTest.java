package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.repository.ReservationTimeRepository;
import roomescape.reservationtime.infra.JdbcReservationTimeRepository;
import roomescape.support.RepositoryTestHelper;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infra.JdbcThemeRepository;

@JdbcTest
public class JdbcThemeRepositoryTest {

    ThemeRepository themeRepository;
    ReservationTimeRepository timeRepository;
    RepositoryTestHelper testHelper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        themeRepository = new JdbcThemeRepository(jdbcTemplate);
        timeRepository = new JdbcReservationTimeRepository(jdbcTemplate);
        testHelper = new RepositoryTestHelper(jdbcTemplate);
    }

    @DisplayName("db의 정상 저장을 테스트 합니다.")
    @Test
    void save_successfully() {
        Theme theme = Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build();

        Theme savedTheme = themeRepository.save(theme);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(savedTheme).isNotNull();
            assertSoftly.assertThat(savedTheme.getId()).isPositive();
            assertSoftly.assertThat(savedTheme.getName()).isEqualTo("theme name");
            assertSoftly.assertThat(savedTheme.getDescription()).isEqualTo("theme description");
            assertSoftly.assertThat(savedTheme.getThumbnailImgUrl()).isEqualTo("theme img url");
        });
    }

    @DisplayName("db에 특정 테마가 존재하지 않는 것을 테스트 합니다.")
    @Test
    void check_none_exists_successfully() {
        Theme theme = Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build();

        Boolean alreadyExists = themeRepository.existsByNameAndDescription(theme);

        assertThat(alreadyExists).isFalse();
    }

    @DisplayName("db에 특정 테마가 존재하는 것을 테스트 합니다.")
    @Test
    void check_exists_successfully() {
        Theme theme1 = Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build();
        themeRepository.save(theme1);

        Theme theme2 = Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build();
        Boolean alreadyExists = themeRepository.existsByNameAndDescription(theme2);
        assertThat(alreadyExists).isTrue();
    }

    @DisplayName("db에서 테마 삭제를 테스트 합니다.")
    @Test
    void delete_theme_successfully() {
        Theme savedTheme = themeRepository.save(Theme.builder()
                .name("theme name")
                .description("theme description")
                .thumbnailImgUrl("theme img url")
                .price(30000L)
                .build());

        assertThatCode(() -> themeRepository.delete(savedTheme.getId())).doesNotThrowAnyException();
    }

    @DisplayName("db에서 테마를 전체 조회합니다.")
    @Test
    void find_all_themes() {
        themeRepository.save(Theme.builder()
                .name("theme name 1")
                .description("theme description 1")
                .thumbnailImgUrl("theme img url 1")
                .price(30000L)
                .build()
        );
        themeRepository.save(Theme.builder()
                .name("theme name 2")
                .description("theme description 2")
                .thumbnailImgUrl("theme img url 2")
                .price(30000L)
                .build()
        );
        themeRepository.save(Theme.builder()
                .name("theme name 3")
                .description("theme description 3")
                .thumbnailImgUrl("theme img url 3")
                .price(30000L)
                .build()
        );

        assertThat(themeRepository.findAll().size()).isEqualTo(3);
    }

    @DisplayName("db에서 최근 7일간 인기있던 테마 상위 10개를 조회를 테스트합니다.")
    @Test
    void find_popular_10_themes_during_recent_7days() {
        ReservationTime time1 = timeRepository.save(ReservationTime.builder()
                .startAt(LocalTime.of(9, 0))
                .build());
        ReservationTime time2 = timeRepository.save(ReservationTime.builder()
                .startAt(LocalTime.of(10, 0))
                .build());

        LocalDate yesterday = LocalDate.now().minusDays(1);
        Theme theme1 = themeRepository.save(Theme.builder().name("theme name 1").description("theme description 1")
                .thumbnailImgUrl("theme img url 1").price(30000L).build());
        Theme theme2 = themeRepository.save(Theme.builder().name("theme name 2").description("theme description 2")
                .thumbnailImgUrl("theme img url 2").price(30000L).build());

        testHelper.insertReservation("스타크", yesterday, theme1.getId(), time1.getId());
        testHelper.insertReservation("카야", yesterday, theme2.getId(), time2.getId());
        testHelper.insertReservation("스타크", yesterday, theme1.getId(), time2.getId());

        LocalDate today = LocalDate.now();
        List<Theme> popularThemes = themeRepository.findSortedPopularThemes(today.minusWeeks(1), today.minusDays(1), 10);

        SoftAssertions.assertSoftly(assertSoftly -> {
            assertSoftly.assertThat(popularThemes.getFirst().getId()).isEqualTo(theme1.getId());
            assertSoftly.assertThat(popularThemes.get(1).getId()).isEqualTo(theme2.getId());
        });
    }
}
