package roomescape.dao.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.domain.Role;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JdbcMemberDao implements MemberDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcMemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member add(Member member) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", member.getEmail());
        parameters.put("password", member.getPassword());
        parameters.put("name", member.getName());
        parameters.put("role", member.getRole());
        Number key = jdbcInsert.executeAndReturnKey(parameters);
        return member.toEntity(key.longValue());
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, mapResultsToMember());
    }

    @Override
    public Member findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, mapResultsToMember(), id);
    }

    @Override
    public Member findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, mapResultsToMember(), email);
    }

    @Override
    public Member findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member WHERE email = ? AND password = ?";
        return jdbcTemplate.queryForObject(sql, mapResultsToMember(), email, password);
    }

    @Override
    public boolean existByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT * FROM member WHERE email = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public boolean existByName(String name) {
        String sql = "SELECT EXISTS(SELECT * FROM member WHERE name = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    private RowMapper<Member> mapResultsToMember() {
        return (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                Role.valueOf(resultSet.getString("role"))
        );
    }
}
