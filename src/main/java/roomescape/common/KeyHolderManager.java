//package roomescape.common;
//
//import java.sql.PreparedStatement;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class KeyHolderManager {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    public KeyHolderManager(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    public Long insertAndReturnId(String sql, SqlConsumer<PreparedStatement> statementConsumer) {
//        KeyHolder keyHolder = new GeneratedKeyHolder();
//        jdbcTemplate.update(connection -> {
//            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
//            statementConsumer.accept(ps);
//            return ps;
//        }, keyHolder);
//        return keyHolder.getKey().longValue();
//    }
//}
