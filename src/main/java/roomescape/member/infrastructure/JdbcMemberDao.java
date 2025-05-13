package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.business.domain.Member;
import roomescape.member.business.domain.Role;
import roomescape.member.business.repository.MemberDao;

@Repository
public class JdbcMemberDao implements MemberDao {

    private final static RowMapper<Member> ROW_MAPPER = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcMemberDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT * FROM member";

        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Member save(final Member member) {
        final String sql = "INSERT INTO member (name, email, password, role) VALUES (:name, :email, :password, :role)";
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().toString());
        jdbcTemplate.update(sql, mapSqlParameterSource, keyHolder);

        final Number key = keyHolder.getKey();
        return new Member(key.longValue(), member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public int deleteById(final Long id) {
        final String sql = "DELETE FROM member WHERE id = :id";
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
    }

    @Override
    public Optional<Member> findById(final Long id) {
        final String sql = "SELECT * FROM member WHERE id = :id";

        try {
            final Member findMember = jdbcTemplate.queryForObject(
                    sql, new MapSqlParameterSource("id", id),
                    ROW_MAPPER);
            return Optional.of(findMember);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        final String sql = "SELECT * FROM member WHERE email = :email AND password = :password";

        final MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("password", password);
        try {
            final Member member = jdbcTemplate.queryForObject(sql, mapSqlParameterSource, ROW_MAPPER);
            return Optional.of(member);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(final String email) {
        final String sql = """
                SELECT EXISTS 
                    (SELECT 1 
                     FROM member 
                     WHERE email = :email
                    )
                """;

        return Boolean.TRUE == jdbcTemplate.queryForObject(
                sql, new MapSqlParameterSource("email", email), Boolean.class);
    }
}
