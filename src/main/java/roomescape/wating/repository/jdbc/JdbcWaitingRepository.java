package roomescape.wating.repository.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.wating.domain.Waiting;
import roomescape.wating.repository.WaitingRepository;

@Repository
@RequiredArgsConstructor
public class JdbcWaitingRepository implements WaitingRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Waiting waiting) {
        final String sql = """
                INSERT INTO waiting(customer_name, reservation_date, time_id, theme_id)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, waiting.getCustomerName().getName());
            ps.setDate(2, Date.valueOf(waiting.getReservationDate()));
            ps.setLong(3, waiting.getTime().getId());
            ps.setLong(4, waiting.getTheme().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("대기 생성에 실패했습니다.");
        }
        return key.longValue();
    }

}
