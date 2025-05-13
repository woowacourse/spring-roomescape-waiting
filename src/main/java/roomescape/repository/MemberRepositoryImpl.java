package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;

@Repository
public class MemberRepositoryImpl implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;

    public MemberRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Member save(
            final MemberEmail email,
            final MemberName name,
            final MemberEncodedPassword password,
            final MemberRole role
    ) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name.name())
                .addValue("email", email.email())
                .addValue("password", password.password())
                .addValue("role", role.name());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Member(id, name, email, password, role);
    }

    public Optional<Member> findByEmail(final MemberEmail email) {
        String sql = "select * from member where email = ? limit 1";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                new MemberName(resultSet.getString("name")),
                new MemberEmail(resultSet.getString("email")),
                new MemberEncodedPassword(resultSet.getString("password")),
                MemberRole.from(resultSet.getString("role"))
        ), email.email()).stream().findFirst();
    }

    public Optional<Member> findById(final Long memberId) {
        String sql = "select * from member where id = ? limit 1";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                new MemberName(resultSet.getString("name")),
                new MemberEmail(resultSet.getString("email")),
                new MemberEncodedPassword(resultSet.getString("password")),
                MemberRole.from(resultSet.getString("role"))
        ), memberId).stream().findFirst();
    }

    public List<Member> findAll() {
        String sql = "select * from member";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                new MemberName(resultSet.getString("name")),
                new MemberEmail(resultSet.getString("email")),
                new MemberEncodedPassword(resultSet.getString("password")),
                MemberRole.from(resultSet.getString("role"))
        ));
    }
}
