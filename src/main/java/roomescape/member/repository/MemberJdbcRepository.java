package roomescape.member.repository;

import static roomescape.member.role.Role.MEMBER;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberRepository;

@Repository
public class MemberJdbcRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Member> memberRowMapper = (result, rowNum) -> new Member(
            result.getLong("id"),
            new Name(result.getString("name")),
            new Email(result.getString("email")),
            new Password(result.getString("password")),
            Role.valueOf(result.getString("role"))
    );


    public MemberJdbcRepository(JdbcTemplate jdbcTemplate,
                                @Qualifier("userJdbcInsert") SimpleJdbcInsert simpleJdbcInsert) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = simpleJdbcInsert;
    }

    @Override
    public Member save(Name name, Email email, Password password) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name.getName())
                .addValue("email", email.getEmail())
                .addValue("password", password.getPassword())
                .addValue("role", "MEMBER");

        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Member(id, name, email, password, MEMBER);
    }

    @Override
    public boolean isExistUser(String email, String password) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND password = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), email, password);
        return !result.isEmpty();
    }

    @Override
    public Member findUserByEmail(String payload) {
        String sql = "SELECT * FROM users WHERE email = ?";
        Member member;
        try {
            member = jdbcTemplate.queryForObject(sql, memberRowMapper, payload);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 회원입니다.");
        }
        return member;
    }

    @Override
    public Member findMemberById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        Member member;
        try {
            member = jdbcTemplate.queryForObject(sql, memberRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 회원입니다.");
        }
        return member;
    }

    @Override
    public Member findMemberByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        Member member;
        try {
            member = jdbcTemplate.queryForObject(sql, memberRowMapper, name);
        } catch (EmptyResultDataAccessException e) {
            throw new IllegalArgumentException("[ERROR] 존재하지 않는 회원입니다.");
        }
        return member;
    }
}
