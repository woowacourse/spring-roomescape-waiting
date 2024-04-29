package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<LoginMember> loginMemberMapper = (rs, rowNum) -> new LoginMember(
        rs.getString("id"),
        rs.getString("email"),
        rs.getString("name"),
        rs.getString("role")
    );

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member WHERE email = ? and password = ?";
        List<Member> result = jdbcTemplate.query(sql, (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            Role.valueOf(rs.getString("role"))
        ), email, password);

        return result.stream().findAny();
    }

    @Override
    public Optional<LoginMember> findById(Long id) {
        String sql = "SELECT id, name, email, role FROM member WHERE id = ?";
        List<LoginMember> result = jdbcTemplate.query(sql, loginMemberMapper, id);
        return result.stream().findAny();
    }

    @Override
    public List<LoginMember> findAll() {
        String sql = "SELECT id, name, email, role FROM member";
        return jdbcTemplate.query(sql, loginMemberMapper);
    }
}
