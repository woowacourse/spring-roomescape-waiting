package roomescape.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Member;
import roomescape.domain.Role;

@Repository
public class MemberDao {

    private static final RowMapper<Member> memberRowMapper = (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("login_id"),
            rs.getString("name"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    public Long save(Member member) {
        return jdbcInsert.executeAndReturnKey(Map.of(
                "login_id", member.getLoginId(),
                "name", member.getName(),
                "password", member.getPassword(),
                "role", member.getRole().name()
        )).longValue();
    }

    public Optional<Member> findById(Long id) {
        String sql = "SELECT id, login_id, name, password, role FROM users WHERE id = ?";
        List<Member> results = jdbcTemplate.query(sql, memberRowMapper, id);
        return results.stream().findFirst();
    }

    public Optional<Member> findByLoginId(String loginId) {
        String sql = "SELECT id, login_id, name, password, role FROM users WHERE login_id = ?";
        List<Member> results = jdbcTemplate.query(sql, memberRowMapper, loginId);
        return results.stream().findFirst();
    }

    public List<Member> findByRole(Role role) {
        String sql = "SELECT id, login_id, name, password, role FROM users WHERE role = ? ORDER BY name, login_id";
        return jdbcTemplate.query(sql, memberRowMapper, role.name());
    }

    public boolean existsByLoginId(String loginId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM users
                    WHERE login_id = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, loginId));
    }
}
