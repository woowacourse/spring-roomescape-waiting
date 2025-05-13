package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;

@Repository
public class ReservationThemeRepositoryImpl implements ReservationThemeRepository {

    private final JdbcTemplate template;

    public ReservationThemeRepositoryImpl(final JdbcTemplate template) {
        this.template = template;
    }

    @Override
    public Optional<ReservationTheme> findById(final Long id) {
        String sql = "SELECT * FROM reservation_theme WHERE id = ?";
        final List<ReservationTheme> reservationThemes = template.query(sql, reservationThemeRowMapper(), id);
        if (reservationThemes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reservationThemes.getFirst());
    }

    @Override
    public List<ReservationTheme> findAll() {
        String sql = "select * from reservation_theme";
        return template.query(sql, reservationThemeRowMapper());
    }

    @Override
    public List<ReservationTheme> findWeeklyThemeOrderByCountDesc() {
        String sql = """
                SELECT th.id, th.name, th.description, th.thumbnail, COUNT(*) AS reservation_count
                FROM reservation_v2 r
                JOIN reservation_theme th ON r.theme_id = th.id
                WHERE PARSEDATETIME(r.date, 'yyyy-MM-dd') BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY', -1, CURRENT_DATE)
                GROUP BY th.id, th.name, th.description, th.thumbnail
                ORDER BY reservation_count DESC
                LIMIT 10; 
                """;
        return template.query(sql, reservationThemeRowMapper());
    }

    @Override
    public ReservationTheme save(final ReservationTheme reservationTheme) {
        String sql = "insert into reservation_theme (name, description, thumbnail) values (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservationTheme.getName());
            ps.setString(2, reservationTheme.getDescription());
            ps.setString(3, reservationTheme.getThumbnail());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservationTheme.toEntity(id);
    }

    @Override
    public int deleteById(final long id) {
        try {
            String sql = "delete from reservation_theme where id = ?";
            return template.update(sql, id);
        } catch (Exception e) {
            throw new IllegalArgumentException("예약 테마를 지울 수 없습니다.");
        }
    }

    @Override
    public boolean existsByName(final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_theme WHERE name = ?)";
        return Boolean.TRUE.equals(template.queryForObject(sql, Boolean.class, name));
    }

    private RowMapper<ReservationTheme> reservationThemeRowMapper() {
        return (rs, rowNum) -> {
            return new ReservationTheme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            );
        };
    }
}
