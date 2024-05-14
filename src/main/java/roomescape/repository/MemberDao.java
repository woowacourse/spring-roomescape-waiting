package roomescape.repository;

import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;

@Repository
public class MemberDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Member> rowMapper =
            (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    Role.valueOf(resultSet.getString("role"))
            );

    public MemberDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Member findById(final Long id) {
        String sql = "select id, name, email, password, role from member where id =?";
        return jdbcTemplate.queryForObject(sql, rowMapper, id);
    }

    public Member findByEmail(final String email) {
        String sql = "select id, name, email, password, role from member where email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, email);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("회원 가입이 되지 않은 사용자입니다");
        }
    }

    public Member findByEmailAndPassword(final String email, final String password) {
        String sql = "select id, name, email, password, role from member where email = ? and password = ?";
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper, email, password);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("회원 가입이 되지 않은 사용자입니다");
        }
    }

    public List<Member> findAll() {
        String sql = "select id, name, email, password, role from member";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
