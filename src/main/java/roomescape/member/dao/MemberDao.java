package roomescape.member.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public class MemberDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Member> rowMapper;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("role")
        );
    }

    public List<Member> findMembers() {
        String sql = "SELECT id, name, email, role FROM member";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public Optional<Member> findMemberById(Long id) {
        String sql = "SELECT id, name, email, role FROM member WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Member> findMemberByEmail(String email) {
        String sql = "SELECT id, name, email, role FROM member WHERE email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, email));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
