//package roomescape.theme.repository;
//
//import java.sql.PreparedStatement;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Repository;
//import roomescape.common.RowMapperManager;
//import roomescape.theme.domain.Theme;
//import roomescape.theme.domain.dto.PopularThemeRequestDto;
//import roomescape.theme.exception.NotFoundThemeException;
//
//@Repository
//public class JdbcThemeRepository implements ThemeRepository {
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Override
//    public Theme save(Theme theme) {
//        Long id = insertWithKeyHolder(theme);
//        return findByIdOrThrow(id);
//    }
//
//    @Override
//    public List<Theme> findThemesOrderByReservationCount(LocalDate from, LocalDate to,
//                                                         PopularThemeRequestDto dto) {
//
//        String sql = String.format("SELECT t.id AS theme_id," +
//                "       t.name AS theme_name," +
//                "       t.description AS theme_description," +
//                "       t.thumbnail AS theme_thumbnail " +
//                "FROM theme AS t INNER JOIN reservation AS r " +
//                "ON r.theme_id = t.id " +
//                "WHERE r.date BETWEEN ? AND ? " +
//                "GROUP BY t.id " +
//                "ORDER BY count(*) %s " +
//                "LIMIT ? ", dto.sortDirection());
//
//        return jdbcTemplate.query(sql, RowMapperManager.themeRowMapper, from, to, dto.size());
//    }
//
//    @Override
//    public Theme findByIdOrThrow(Long id) {
//        return findById(id)
//                .orElseThrow(() -> new NotFoundThemeException("해당 테마 id가 존재하지 않습니다."));
//    }
//
//    @Override
//    public Optional<Theme> findById(Long id) {
//        String sql = "select id AS theme_id, name AS theme_name, description AS theme_description, thumbnail AS theme_thumbnail from theme where id = ?";
//
//        try {
//            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RowMapperManager.themeRowMapper, id));
//        } catch (EmptyResultDataAccessException e) {
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public void deleteById(Long id) {
//        String sql = "delete from theme where id = ?";
//        jdbcTemplate.update(sql, id);
//    }
//
//    private Long insertWithKeyHolder(Theme theme) {
//        String sql = "INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)";
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//
//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(
//                    sql,
//                    new String[]{"id"});
//            ps.setString(1, theme.getName());
//            ps.setString(2, theme.getDescription());
//            ps.setString(3, theme.getThumbnail());
//            return ps;
//        }, keyHolder);
//
//        return keyHolder.getKey().longValue();
//    }
//
//    public List<Theme> findAll() {
//        String sql = "SELECT id AS theme_id, name AS theme_name, description AS theme_description, thumbnail AS theme_thumbnail FROM theme";
//
//        return jdbcTemplate.query(sql, RowMapperManager.themeRowMapper);
//    }
//}
