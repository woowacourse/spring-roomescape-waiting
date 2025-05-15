package roomescape.member.repository;

import static roomescape.member.role.Role.MEMBER;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.role.Role;
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
    public Member save(Member member) {
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", "MEMBER");

        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Member(id,
                new Name(member.getName()),
                new Email(member.getEmail()),
                new Password(member.getPassword()),
                MEMBER);
    }

    @Override
    public boolean existsByEmailAndPassword(Email email, Password password) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND password = ? LIMIT 1";
        List<Integer> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), email.getEmail(),
                password.getPassword());
        return !result.isEmpty();
    }

    @Override
    public Optional<Member> findByEmail(Email payload) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, memberRowMapper, payload.getEmail())
                .stream().findFirst();
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, memberRowMapper, id)
                .stream().findFirst();
    }

    @Override
    public Optional<Member> findByName(Name name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        return jdbcTemplate.query(sql, memberRowMapper, name.getName())
                .stream().findFirst();
    }

    @Override
    public List<Member> findAll() {
        return List.of();
    }
}
