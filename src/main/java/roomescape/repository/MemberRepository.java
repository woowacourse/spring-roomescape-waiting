package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );


    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Member> findById(long id) {
        String sql = "SELECT id, name, email, password, role " +
                "FROM member " +
                "WHERE id = ?";
        List<Member> members = jdbcTemplate.query(sql, memberRowMapper, id);
        return members.isEmpty() ? Optional.empty() : Optional.of(members.get(0));
    }

    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT id, name, email, password, role " +
                "FROM member " +
                "WHERE email = ? " +
                "AND password = ?";
        List<Member> members = jdbcTemplate.query(sql, memberRowMapper, email, password);
        return members.isEmpty() ? Optional.empty() : Optional.of(members.get(0));
    }

    public List<Member> findAll() {
        String sql = "SELECT id, name, email, password, role " +
                "FROM member ";
        return jdbcTemplate.query(sql, memberRowMapper);
    }
}
