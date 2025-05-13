package roomescape.dao.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.custom.NotFoundException;

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
    public List<Member> findAllMembers() {
        String sql = "SELECT id, name, email, password FROM member";
        return jdbcTemplate.query(sql, createMemberMapper());
    }

    public Member findMemberByEmail(String email) {
        String sql = "SELECT id, name, email, password FROM member WHERE email = ?";
        try {
            return jdbcTemplate.queryForObject(sql, createMemberMapper(), email);
        } catch (DataAccessException e) {
            throw new NotFoundException("member");
        }
    }

    public Member findMemberById(Long id) {
        String sql = "SELECT id, name, email, password FROM member WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, createMemberMapper(), id);
        } catch (DataAccessException e) {
            throw new NotFoundException("member");
        }
    }

    public boolean existMemberByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT id FROM member WHERE email = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    public Member addMember(Member member) {
        Map<String, Object> param = new HashMap<>();
        param.put("name", member.getName());
        param.put("email", member.getEmail());
        param.put("password", member.getPassword());

        Number key = jdbcInsert.executeAndReturnKey(param);
        return new Member(key.longValue(), member.getName(), member.getEmail(),
            member.getPassword());
    }

    private RowMapper<Member> createMemberMapper() {
        return (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"));
    }
}
