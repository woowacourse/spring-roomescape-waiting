package roomescape.domain.reservation.infrastructure.db.dao;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.model.entity.ReservationTheme;

@Component
@RequiredArgsConstructor
public class ReservationThemeH2Dao implements ReservationThemeDao {

    private static final RowMapper<ReservationTheme> DEFAULT_ROW_MAPPER = (resultSet, rowNum) ->
            ReservationTheme.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .description(resultSet.getString("description"))
                    .thumbnail(resultSet.getString("thumbnail"))
                    .build();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationTheme> selectAll() {
        String selectQuery = """
                SELECT id, name, description, thumbnail
                FROM theme
                """;

        return jdbcTemplate.query(selectQuery, DEFAULT_ROW_MAPPER);
    }

    @Override
    public ReservationTheme insertAndGet(ReservationTheme reservationTheme) {
        String insertQuery = """
                INSERT INTO theme (name, description, thumbnail)
                VALUES (:name, :description, :thumbnail)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservationTheme.getName())
                .addValue("description", reservationTheme.getDescription())
                .addValue("thumbnail", reservationTheme.getThumbnail());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(insertQuery, params, keyHolder, new String[]{"id"});

        Long id = keyHolder.getKey().longValue();
        return reservationTheme.assignId(id);
    }

    @Override
    public Optional<ReservationTheme> selectById(Long id) {
        String selectQuery = """
                SELECT id, name, description, thumbnail
                FROM theme
                WHERE id = :id
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(selectQuery, Map.of("id", id), DEFAULT_ROW_MAPPER));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteQuery = "DELETE FROM theme WHERE id = :id";
        jdbcTemplate.update(deleteQuery, Map.of("id", id));
    }

    @Override
    public List<ReservationTheme> getOrderByThemeBookedCountWithLimit(int limit) {
        String query = """
                SELECT th.id, th.name, th.description, th.thumbnail
                FROM reservation r
                INNER JOIN theme th ON r.theme_id = th.id
                GROUP BY r.theme_id
                ORDER BY count(r.theme_id) DESC
                LIMIT :limit
                """;

        return jdbcTemplate.query(query, Map.of("limit", limit), DEFAULT_ROW_MAPPER);
    }
}
