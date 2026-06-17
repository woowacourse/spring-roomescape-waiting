package roomescape.dao.jdbc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.ThemeDao;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeReservationCount;
import roomescape.common.vo.Name;
import roomescape.dto.response.AvailableTimeResponseDto;
import roomescape.dto.response.TimeResponseDto;


@Repository
public class ThemeJdbcDao implements ThemeDao {
    // 활성 예약의 deleted_at 기본값(soft-delete sentinel). 취소된 예약은 실제 취소 시각을 가진다.
    private static final LocalDateTime SENTINEL = LocalDateTime.of(9999, 12, 31, 0, 0, 0);

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            new Theme(
                    rs.getLong("id"),
                    new Name(rs.getString("name")),
                    rs.getString("thumbnail_url"),
                    rs.getString("description"),
                    rs.getLong("price")
            );

    private static final RowMapper<ThemeReservationCount> THEME_COUNT_ROW_MAPPER = (rs, rowNum) ->
            new ThemeReservationCount(
                    THEME_ROW_MAPPER.mapRow(rs, rowNum),
                    rs.getLong("reservation_count")
            );

    private static final RowMapper<AvailableTimeResponseDto> AVAILABLE_TIME_MAPPER = (rs, rowNum) ->
            new AvailableTimeResponseDto(
                    new TimeResponseDto(rs.getLong("time_id"), LocalTime.parse(rs.getString("time_start_at"))),
                    rs.getBoolean("already_booked")
            );

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("themes")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                SELECT
                    id,
                    name,
                    thumbnail_url,
                    description,
                    price
                FROM themes
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
                SELECT
                     id,
                     name,
                     thumbnail_url,
                     description,
                     price
                 FROM themes
                 WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.query(sql, params, THEME_ROW_MAPPER).stream().findFirst();
    }

    @Override
    public Theme insert(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName().getValue())
                .addValue("thumbnail_url", theme.getThumbnailUrl())
                .addValue("description", theme.getDescription())
                .addValue("price", theme.getPrice());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new Theme(id, theme.getName(), theme.getThumbnailUrl(), theme.getDescription(), theme.getPrice());
    }

    @Override
    public Theme update(Theme theme) {
        String sql = """
                UPDATE themes
                SET name = :name, thumbnail_url = :thumbnailUrl, description = :description, price = :price
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName().getValue())
                .addValue("thumbnailUrl", theme.getThumbnailUrl())
                .addValue("description", theme.getDescription())
                .addValue("price", theme.getPrice())
                .addValue("id", theme.getId());
        jdbcTemplate.update(sql, params);
        return theme;
    }

    @Override
    public boolean delete(Long id) {
        String sql = """
                DELETE FROM themes
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return jdbcTemplate.update(sql, params) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM themes WHERE id = :id
                )
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public boolean existsByName(Name name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM themes WHERE name = :name
                )
                """;
        SqlParameterSource params = new MapSqlParameterSource("name", name.getValue());

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public List<AvailableTimeResponseDto> findAvailableTimesById(Long themeId, LocalDate localDate) {
        String sql = """
                SELECT
                    t.id as time_id,
                    t.start_at as time_start_at,
                    EXISTS(
                        SELECT 1 FROM reservations r
                        WHERE r.time_id = t.id
                        AND r.theme_id = :themeId
                        AND r.date = :date
                        AND r.deleted_at = :sentinel
                    ) as already_booked
                 FROM times t
                 ORDER BY t.start_at
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("date", localDate)
                .addValue("sentinel", SENTINEL);

        return jdbcTemplate.query(sql, params, AVAILABLE_TIME_MAPPER);
    }

    @Override
    public List<ThemeReservationCount> findReservationCounts(LocalDate from, LocalDate to) {
        String sql = """
                    SELECT
                        th.id,
                        th.name,
                        th.thumbnail_url,
                        th.description,
                        th.price,
                        COALESCE(r.cnt, 0) AS reservation_count
                    FROM themes th
                    LEFT JOIN (
                        SELECT theme_id, COUNT(*) AS cnt
                        FROM reservations
                        WHERE date BETWEEN :from AND :to
                        GROUP BY theme_id
                    ) r ON th.id = r.theme_id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", from)
                .addValue("to", to);

        return jdbcTemplate.query(sql, params, THEME_COUNT_ROW_MAPPER);
    }
}
