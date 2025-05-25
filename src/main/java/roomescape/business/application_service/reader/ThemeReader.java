package roomescape.business.application_service.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.dto.ThemeDto;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThemeReader {

    private final JdbcClient jdbcClient;

    public List<ThemeDto> getAll() {
        return jdbcClient.sql("SELECT * FROM theme")
                .query((rs, rowNum) -> new ThemeDto(
                        Id.create(rs.getString("id")),
                        new ThemeName(rs.getString("theme_name")),
                        rs.getString("description"),
                        rs.getString("thumbnail")
                ))
                .list();
    }

    private static final int AGGREGATE_START_DATE_INTERVAL = 7;
    private static final int AGGREGATE_END_DATE_INTERVAL = 1;

    public List<ThemeDto> getPopulars(final int size) {
        LocalDate now = LocalDate.now();
        String sql = """
                SELECT t.*
                FROM reservation r
                JOIN reservation_slot rs ON rs.id = r.slot_id
                JOIN theme t ON t.id = rs.theme_id
                WHERE rs.reservation_date >= :start AND rs.reservation_date <= :end
                GROUP BY t.id
                ORDER BY COUNT(*) DESC
                LIMIT :size;
                """;
        return jdbcClient.sql(sql)
                .param("start", now.minusDays(AGGREGATE_START_DATE_INTERVAL))
                .param("end", now.minusDays(AGGREGATE_END_DATE_INTERVAL))
                .param("size", size)
                .query((rs, rowNum) -> new ThemeDto(
                        Id.create(rs.getString("id")),
                        new ThemeName(rs.getString("name")),
                        rs.getString("description"),
                        rs.getString("thumbnail")
                ))
                .list();
    }
}
