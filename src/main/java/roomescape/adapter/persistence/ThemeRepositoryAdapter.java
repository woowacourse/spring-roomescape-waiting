package roomescape.adapter.persistence;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;
import roomescape.domain.repository.projection.PopularThemeProjection;

@Repository
public class ThemeRepositoryAdapter implements ThemeRepository {

    private final ThemeJpaRepository jpaRepository;
    // 과도기 전용: findPopularBetween 은 reservation(미엔티티) 집계라 JPQL 불가 -> 기존 SQL 유지.
    // 1-2/3-2 에서 Reservation 엔티티화 후 JPQL 집계로 승격 예정.
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<PopularThemeProjection> POPULAR_ROW_MAPPER = (rs, rowNum) ->
            new PopularThemeProjection(
                    Theme.withId(
                            rs.getLong("id"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getString("thumbnail_url")
                    ),
                    rs.getLong("reservation_count")
            );

    public ThemeRepositoryAdapter(ThemeJpaRepository jpaRepository, JdbcTemplate jdbcTemplate) {
        this.jpaRepository = jpaRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Theme save(Theme theme) {
        return jpaRepository.save(theme);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<PopularThemeProjection> findPopularBetween(LocalDate from, LocalDate to, int limit) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_url,
                       COUNT(r.id) AS reservation_count
                FROM theme t
                INNER JOIN reservation r ON t.id = r.theme_id
                WHERE r.date >= ?
                  AND r.date <  ?
                GROUP BY t.id, t.name, t.description, t.thumbnail_url
                ORDER BY reservation_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, POPULAR_ROW_MAPPER,
                Date.valueOf(from), Date.valueOf(to), limit);
    }
}
