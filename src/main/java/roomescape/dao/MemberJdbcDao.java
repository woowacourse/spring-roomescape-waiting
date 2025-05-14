package roomescape.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.model.Member;
import roomescape.model.Role;

@Repository
public class MemberJdbcDao implements MemberDao {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    Role.fromValue(resultSet.getString("role"))
            );

    private final JdbcTemplate jdbcTemplate;

    public MemberJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER, email).stream().findAny();
    }

    @Override
    public Optional<Member> findById(final Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER, id).stream().findAny();
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }
}
