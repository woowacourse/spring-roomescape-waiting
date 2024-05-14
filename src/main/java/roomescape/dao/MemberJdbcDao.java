package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Name;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MemberJdbcDao implements MemberDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Member> rowMapper = (resultSet, rowNumber) -> new Member(
            resultSet.getLong("id"),
            new Name(resultSet.getString("name")),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role")));

    public MemberJdbcDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(final Member member) {
        final Name name = member.getName();
        final String email = member.getEmail();
        final String password = member.getPassword();
        final Role role = member.getRole();
        final Long id = jdbcInsert.executeAndReturnKey(
                        Map.of("name", name.getName(),
                                "email", email,
                                "password", password,
                                "role", role))
                .longValue();
        return new Member(Objects.requireNonNull(id), name, email, password, role);
    }

    @Override
    public Optional<Member> findById(final Long id) {
        final String sql = "SELECT * FROM member WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = "SELECT * FROM member WHERE email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, rowMapper);
    }
}
