package roomescape.utils;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public final class JdbcTemplateUtils {

    private JdbcTemplateUtils() {
    }

    public static void deleteAllTables(JdbcTemplate jdbcTemplate) {
        try {
            jdbcTemplate.update("delete from reservation");
            jdbcTemplate.update("delete from reservation_time");
            jdbcTemplate.update("delete from theme");
            jdbcTemplate.update("delete from member");
        } catch (DataAccessException e) {
            throw new RuntimeException("테이블 삭제 중 오류 발생", e);
        }
    }
}
