package roomescape.dao.jdbc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.dao.WaitingDao;
import roomescape.domain.Waiting;

public class WaitingJdbcDao implements WaitingDao {
    private static final RowMapper<Waiting> ROW_MAPPER = (rs, rowNum) -> new Waiting(
            rs.getLong("id"),
            rs.getLong("member_id"),
            LocalDate.parse(rs.getString("date")),
            rs.getLong("time_id"),
            rs.getLong("theme_id")
    );
    private static final String BASE_SELECT = """
            SELECT * FROM WAITINGS
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waitings")
                .usingGeneratedKeyColumns("id")
                .usingColumns("id", "member_id", "date", "time_id", "theme_id", "order");
    }

    @Override
    public List<Waiting> findAll() {
        return jdbcTemplate.query(BASE_SELECT, ROW_MAPPER);
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        String sql = BASE_SELECT + "WHERE id = :id";
        MapSqlParameterSource parameterSource = new MapSqlParameterSource("id", id);

        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, parameterSource, ROW_MAPPER));
    }

    @Override
    public Waiting insert(Waiting waiting) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", waiting.getId())
                .addValue("member_id", waiting.getMemberId())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTimeId())
                .addValue("theme_id", waiting.getThemeId());

        long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new Waiting(
                id, waiting.getMemberId(), waiting.getDate(),
                waiting.getTimeId(), waiting.getThemeId());
    }

    @Override
    public Waiting update(Waiting waiting) {
        String sql = """
                UPDATE waitings
                SET member_id = :memberId, date = :date, time_id = :timeId, theme_id = :themeId
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", waiting.getId())
                .addValue("member_id", waiting.getMemberId())
                .addValue("date", waiting.getDate())
                .addValue("time_id", waiting.getTimeId())
                .addValue("theme_id", waiting.getThemeId());

        jdbcTemplate.update(sql, params);
        return waiting;
    }

    @Override
    public int delete(Long id) {
        String sql = """
                DELETE FROM waitings
                WHERE id = :id
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return jdbcTemplate.update(sql, params);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS(SELECT 1 FROM waitings WHERE id = :id);
                """;
        SqlParameterSource params = new MapSqlParameterSource("id", id);
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, params, Boolean.class));
    }
}
