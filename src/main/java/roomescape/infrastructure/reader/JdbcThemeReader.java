package roomescape.infrastructure.reader;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.application_service.reader.ThemeReader;
import roomescape.business.dto.ThemeDto;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JdbcThemeReader implements ThemeReader {

    private final JdbcClient jdbcClient;

    @Override
    public List<ThemeDto> getAll() {
        String sql = "SELECT * FROM theme";

        return jdbcClient.sql(sql)
                .query(ThemeDto.ROW_MAPPER)
                .list();
    }

    private static final int AGGREGATE_START_DATE_INTERVAL = 7;
    private static final int AGGREGATE_END_DATE_INTERVAL = 1;

    @Override
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
                .query(ThemeDto.ROW_MAPPER)
                .list();
    }
}
