package roomescape.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcThemeRepositoryTest {

    private static final DataSource TEST_DATASOURCE = DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:database-test")
            .username("sa")
            .build();


    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(TEST_DATASOURCE);
    private final JdbcThemeRepository themeRepository = new JdbcThemeRepository(jdbcTemplate);


    @BeforeEach
    void setUp() {
        DatabaseInitializationSettings settings = new DatabaseInitializationSettings();
        settings.setSchemaLocations(List.of("classpath:schema.sql"));
        SqlDataSourceScriptDatabaseInitializer sqlDataSourceScriptDatabaseInitializer =
                new SqlDataSourceScriptDatabaseInitializer(TEST_DATASOURCE, settings);
        sqlDataSourceScriptDatabaseInitializer.initializeDatabase();
    }

    @Test
    void 테마를_저장할_수_있다() {
        //when
        Long createdId = themeRepository.create(new Theme("test1", "description1", "thumbnail1"));

        //then
        assertThat(themeRepository.findById(createdId))
                .hasValue(new Theme(1L, "test1", "description1", "thumbnail1"));
    }

    @Test
    void id로_테마_조회할_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('test1', 'description1', 'thumbnail1')");

        //when
        Optional<Theme> theme = themeRepository.findById(1L);

        //then
        assertThat(theme).hasValue(new Theme(1L, "test1", "description1", "thumbnail1"));
    }

    @Test
    void id값이_없다면_빈_Optional_값이_반환된다() {
        //when
        Optional<Theme> theme = themeRepository.findById(1L);

        //then
        assertThat(theme).isEmpty();
    }

    @Test
    void 전체_테마_목록을_조회할_수_있다() {
        //given
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('test1', 'description1', 'thumbnail1')");
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('test2', 'description2', 'thumbnail2')");

        //when
        List<Theme> themes = themeRepository.findAll();

        //then
        assertThat(themes).isEqualTo(List.of(
                new Theme(1L, "test1", "description1", "thumbnail1"),
                new Theme(2L, "test2", "description2", "thumbnail2")
        ));
    }

    @Test
    void id값으로_테마를_삭제한다() {
        //given
        jdbcTemplate.update("INSERT INTO theme(name, description, thumbnail) VALUES ('test1', 'description1', 'thumbnail1')");

        //when
        themeRepository.deleteById(1L);

        //then
        assertThat(themeRepository.findById(1L)).isEmpty();
    }
}