package roomescape.waiting.infrastructure;

import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingRepository;

@Repository
public class WaitingJdbcTemplateRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public WaitingJdbcTemplateRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("waiting")
                .usingGeneratedKeyColumns("id");
    }


    @Override
    public Waiting save(Waiting waiting) {
        Map<String, Object> params = Map.of(
                "name", waiting.getName(),
                "date", waiting.getDate(),
                "time_id", waiting.getTime().getId(),
                "theme_id", waiting.getTheme().getId(),
                "created_at", waiting.getCreatedAt()
        );
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return waiting.appendId(id);
    }
}
