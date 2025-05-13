package roomescape.member.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;

@Repository
public class JdbcMemberDao implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert memberInserter;
    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("role")
            );

    public JdbcMemberDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberInserter = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(final Member member) {
        final Map<String, Object> parameters = Map.of(
                "name", member.getName(),
                "email", member.getEmail(),
                "password", member.getPassword(),
                "role", member.getRole().name()
        );
        final long id = memberInserter.executeAndReturnKey(parameters).longValue();
        return member.withId(id);
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT id, name, email, password, role FROM member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    @Override
    public Optional<Member> findById(final long id) {
        final String sql = "SELECT id, name, email, password, role FROM member WHERE id = ?";
        return jdbcTemplate.query(sql, memberRowMapper, id).stream().findFirst();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(final MemberEmail email, final String password) {
        final String sql = "SELECT id, name, email, password, role FROM member WHERE email = ? AND password = ?";
        return jdbcTemplate.query(sql, memberRowMapper, email.getEmail(), password).stream().findFirst();
    }

    @Override
    public boolean existsByEmail(final MemberEmail email) {
        final String sql = "SELECT 1 FROM member WHERE email = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql,
                (resultSet, rowNumber) -> resultSet.getInt(1),
                email.getEmail());
        return !result.isEmpty();
    }

    @Override
    public boolean existsByName(final MemberName name) {
        final String sql = "SELECT 1 FROM member WHERE name = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql,
                (resultSet, rowNumber) -> resultSet.getInt(1),
                name.getName());
        return !result.isEmpty();
    }
}
