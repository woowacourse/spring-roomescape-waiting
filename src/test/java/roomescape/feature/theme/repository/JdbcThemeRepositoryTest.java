package roomescape.feature.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.feature.theme.domain.Theme;
import roomescape.feature.theme.domain.ThemeStatus;
import roomescape.global.error.exception.GeneralException;

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class JdbcThemeRepositoryTest {

    private static volatile boolean saveSucceeded = false;
    private static volatile boolean findSucceeded = false;

    private JdbcThemeRepository themeRepository;
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert timeInsert;
    private int timeSequence;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        themeRepository = new JdbcThemeRepository(dataSource);
        jdbcTemplate = new JdbcTemplate(dataSource);
        timeInsert = new SimpleJdbcInsert(dataSource)
            .withTableName("reservation_time")
            .usingColumns("start_at")
            .usingGeneratedKeyColumns("id");
        timeSequence = 0;
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 테마_저장 {

        @Test
        @Order(1)
        void 테마를_저장한다() {
            // given
            String name = "테마1";
            String description = "설명1";
            String imageUrl = "image1";
            Theme theme = Theme.create(name, description, imageUrl);

            // when
            Long savedThemeId = themeRepository.save(theme).getId();

            Theme expectedSavedTheme = Theme.reconstruct(savedThemeId, name, description, imageUrl, ThemeStatus.ACTIVE);

            // then
            Theme actualSavedTheme = jdbcTemplate.queryForObject(
                "SELECT id, name, description, image_url, status FROM theme WHERE id = ?",
                (rs, rowNum) -> Theme.reconstruct(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("image_url"),
                    ThemeStatus.valueOf(rs.getString("status"))
                ),
                savedThemeId
            );

            assertThat(actualSavedTheme).usingRecursiveComparison()
                .isEqualTo(expectedSavedTheme);

            saveSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제되지_않은_같은_이름의_테마는_중복_저장할_수_없다() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");

            // given
            themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

            // when & then
            assertThatThrownBy(() -> themeRepository.save(Theme.create("테마1", "설명2", "image2.png")))
                .isInstanceOf(DuplicateKeyException.class);
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class 테마_목록_조회 {

        @BeforeEach
        void assumeSaveWorks() {
            Assumptions.assumeTrue(saveSucceeded, "save 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        @Order(1)
        void 활성_테마를_조회한다() {
            // given
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));

            // when
            List<Theme> actual = themeRepository.findAllByNotDeleted();

            // then
            assertThat(actual)
                .extracting(Theme::getId, Theme::getName, Theme::getDescription, Theme::getImageUrl)
                .containsExactly(
                    tuple(theme1.getId(), "테마1", "설명1", "image1.png"),
                    tuple(theme2.getId(), "테마2", "설명2", "image2.png")
                );
            findSucceeded = true;
        }

        @Test
        @Order(2)
        void 삭제된_테마는_조회하지_않는다() {
            // given
            Theme deletedTheme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme activeTheme = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            themeRepository.deleteThemeById(deletedTheme.getId());

            // when
            List<Theme> actual = themeRepository.findAllByNotDeleted();

            // then
            assertThat(actual)
                .extracting(Theme::getId, Theme::getName, Theme::getDescription, Theme::getImageUrl)
                .containsExactly(tuple(activeTheme.getId(), "테마2", "설명2", "image2.png"));
        }
    }

    @Nested
    class 테마_ID_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 테마를_ID로_조회한다() {
            // given
            Theme savedTheme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

            // when
            Optional<Theme> actual = themeRepository.findThemeByIdAndNotDeleted(savedTheme.getId());

            // then
            assertThat(actual).isPresent();
            assertThat(actual.get().getId()).isEqualTo(savedTheme.getId());
            assertThat(actual.get().getName()).isEqualTo("테마1");
        }

        @Test
        void 존재하지_않으면_빈_값을_반환한다() {
            // when
            Optional<Theme> actual = themeRepository.findThemeByIdAndNotDeleted(1L);

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class 테마_삭제 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 테마를_소프트_삭제한다() {
            // given
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));

            // when
            themeRepository.deleteThemeById(theme1.getId());

            // then
            assertThat(themeRepository.findAllByNotDeleted())
                .extracting(Theme::getId)
                .containsExactly(theme2.getId());
            assertThat(countDeletedThemeById(theme1.getId())).isEqualTo(1);
            assertThat(themeRepository.findThemeByIdAndNotDeleted(theme1.getId())).isEmpty();
            assertThat(themeRepository.existsThemeByIdAndNotDeleted(theme1.getId())).isFalse();
        }

        @Test
        void 이미_삭제된_테마를_삭제하면_예외가_발생한다() {
            // given
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            themeRepository.deleteThemeById(theme.getId());

            // when & then
            assertThatThrownBy(() -> themeRepository.deleteThemeById(theme.getId()))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마를 찾을 수 없습니다.");
        }
    }

    @Nested
    class 테마_ID_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 존재하면_true를_반환한다() {
            // given
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

            // when
            boolean actual = themeRepository.existsThemeByIdAndNotDeleted(theme.getId());

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = themeRepository.existsThemeByIdAndNotDeleted(1L);

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class 테마_이름_존재_여부_확인 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 해당_이름의_테마가_존재하면_true를_반환한다() {
            // given
            themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));

            // when
            boolean actual = themeRepository.existsThemeByNameAndNotDeleted("테마1");

            // then
            assertThat(actual).isTrue();
        }

        @Test
        void 해당_이름의_테마가_존재하지_않으면_false를_반환한다() {
            // when
            boolean actual = themeRepository.existsThemeByNameAndNotDeleted("테마1");

            // then
            assertThat(actual).isFalse();
        }

        @Test
        void 삭제된_테마의_이름이면_false를_반환한다() {
            // given
            Theme theme = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            themeRepository.deleteThemeById(theme.getId());

            // when
            boolean actual = themeRepository.existsThemeByNameAndNotDeleted("테마1");

            // then
            assertThat(actual).isFalse();
        }
    }

    @Nested
    class 인기_테마_조회 {

        @BeforeEach
        void assumeBasicsWork() {
            Assumptions.assumeTrue(saveSucceeded && findSucceeded, "기본 기능이 동작하지 않아 건너뜁니다.");
        }

        @Test
        void 예약_개수가_많은_순서대로_조회한다() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 7);
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Theme theme3 = themeRepository.save(Theme.create("테마3", "설명3", "image3.png"));
            saveReservations(theme1, startDate, 3);
            saveReservations(theme2, startDate, 5);
            saveReservations(theme3, startDate, 1);

            // when
            List<Theme> actual = themeRepository.findPopularThemesDateBetween(startDate, endDate, 10);

            // then
            assertThat(actual)
                .extracting(Theme::getName)
                .containsExactly("테마2", "테마1", "테마3");
        }

        @Test
        void 조회_기간_밖의_예약은_인기순에_반영하지_않는다() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 7);
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            saveReservations(theme1, startDate.minusDays(1), 8);
            saveReservations(theme1, startDate, 1);
            saveReservations(theme2, startDate, 2);

            // when
            List<Theme> actual = themeRepository.findPopularThemesDateBetween(startDate, endDate, 10);

            // then
            assertThat(actual)
                .extracting(Theme::getName)
                .containsExactly("테마2", "테마1");
        }

        @Test
        void limit_개수만큼_조회한다() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 7);
            for (int i = 1; i <= 12; i++) {
                Theme theme = themeRepository.save(Theme.create("테마" + i, "설명" + i, "image" + i + ".png"));
                saveReservations(theme, startDate, i);
            }

            // when
            List<Theme> actual = themeRepository.findPopularThemesDateBetween(startDate, endDate, 10);

            // then
            assertThat(actual).hasSize(10);
            assertThat(actual)
                .extracting(Theme::getName)
                .containsExactly("테마12", "테마11", "테마10", "테마9", "테마8", "테마7", "테마6", "테마5", "테마4", "테마3");
        }

        @Test
        void 삭제된_테마와_삭제된_예약은_인기순에_반영하지_않는다() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 7);
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            Theme theme3 = themeRepository.save(Theme.create("테마3", "설명3", "image3.png"));
            saveReservations(theme1, startDate, 3);
            List<Long> theme2ReservationIds = saveReservations(theme2, startDate, 5);
            saveReservations(theme3, startDate, 7);
            themeRepository.deleteThemeById(theme3.getId());
            deleteReservation(theme2ReservationIds.get(0));
            deleteReservation(theme2ReservationIds.get(1));
            deleteReservation(theme2ReservationIds.get(2));

            // when
            List<Theme> actual = themeRepository.findPopularThemesDateBetween(startDate, endDate, 10);

            // then
            assertThat(actual)
                .extracting(Theme::getName)
                .containsExactly("테마1", "테마2");
        }

        @Test
        void 삭제된_시간에_연결된_예약은_인기순에_반영하지_않는다() {
            // given
            LocalDate startDate = LocalDate.of(2026, 5, 1);
            LocalDate endDate = LocalDate.of(2026, 5, 7);
            Theme theme1 = themeRepository.save(Theme.create("테마1", "설명1", "image1.png"));
            Theme theme2 = themeRepository.save(Theme.create("테마2", "설명2", "image2.png"));
            saveReservations(theme1, startDate, 2);
            Long deletedTimeId = saveTime(LocalTime.of(15, 0));
            saveReservation("삭제된시간예약자", startDate, deletedTimeId, theme2.getId());
            deleteTime(deletedTimeId);

            // when
            List<Theme> actual = themeRepository.findPopularThemesDateBetween(startDate, endDate, 10);

            // then
            assertThat(actual)
                .extracting(Theme::getName)
                .containsExactly("테마1");
        }
    }

    private List<Long> saveReservations(Theme theme, LocalDate date, int count) {
        List<Long> reservationIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Long timeId = saveTime(LocalTime.of(10, 0).plusMinutes(timeSequence++));
            reservationIds.add(saveReservation("예약자" + theme.getId() + "-" + i, date, timeId, theme.getId()));
        }
        return reservationIds;
    }

    private Long saveTime(LocalTime startAt) {
        return timeInsert.executeAndReturnKey(Map.of("start_at", startAt)).longValue();
    }

    private Long saveReservation(String name, LocalDate date, Long timeId, Long themeId) {
        return new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("reservation")
            .usingColumns("name", "date", "time_id", "theme_id")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "name", name,
                "date", date,
                "time_id", timeId,
                "theme_id", themeId
            ))
            .longValue();
    }

    private void deleteReservation(Long id) {
        jdbcTemplate.update("UPDATE reservation SET status = 'DELETED' WHERE id = ?", id);
    }

    private void deleteTime(Long id) {
        jdbcTemplate.update("UPDATE reservation_time SET status = 'DELETED' WHERE id = ?", id);
    }

    private Integer countDeletedThemeById(Long id) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM theme WHERE id = ? AND status = 'DELETED'",
            Integer.class,
            id
        );
    }
}
