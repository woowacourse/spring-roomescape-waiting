package roomescape.member.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.exception.MemberNotExistException;
import roomescape.member.model.Member;
import roomescape.member.model.Role;

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
        parameters.put("role", member.getRole().name());
        Number key = jdbcInsert.executeAndReturnKey(parameters);
        return member.toEntity(key.longValue());
    }

    @Override
    public List<Member> findAll() {
        String sql = generateFindAllQuery();
        return jdbcTemplate.query(sql, mapResultsToMember());
    }

    private String generateFindAllQuery() {
        return "SELECT * FROM member";
    }

    @Override
    public Member findById(Long id) {
        String whereClause = " WHERE id = ?";
        String sql = generateFindAllQuery() + whereClause;
        try {
            return jdbcTemplate.queryForObject(sql, mapResultsToMember(), id);
        } catch (DataAccessException exception) {
            throw new MemberNotExistException();
        }
    }

    @Override
    public Member findByEmail(String email) {
        String whereClause = " WHERE email = ?";
        String sql = generateFindAllQuery() + whereClause;
        return jdbcTemplate.queryForObject(sql, mapResultsToMember(), email);
    }

    @Override
    public Member findByEmailAndPassword(String email, String password) {
        String whereClause = " WHERE email = ? AND password = ?";
        String sql = generateFindAllQuery() + whereClause;
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
