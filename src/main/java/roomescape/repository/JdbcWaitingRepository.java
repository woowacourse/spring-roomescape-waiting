package roomescape.repository;

import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.service.dto.WaitingCommand;

@Repository
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcWaitingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int calculateWaitingNumberByName(WaitingCommand waiting) {
        String sql = """
                
                """;
        return 0;
    }

    @Override
    public void insert(WaitingCommand waiting) {
        SimpleJdbcInsert insert = createInsert();
        Map<String, Object> params = createParams(waiting);
        insert.execute(params);
    }

    @Override
    public void delete(WaitingCommand waiting) {
        String sql = """
                
                """;

    }

    private SimpleJdbcInsert createInsert() {
        return new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("waiting");
    }

    private Map<String, Object> createParams(WaitingCommand waiting) {
        return Map.of(
                "name", waiting.name(),
                "date", waiting.date(),
                "time_id", waiting.timeId(),
                "theme_id", waiting.themeId()
        );
    }
}
