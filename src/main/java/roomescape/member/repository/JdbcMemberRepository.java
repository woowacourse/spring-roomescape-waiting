package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> memberRowMapper = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("role")
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("member")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "email", "password", "role");
    }

    @Override
    public Optional<Member> findById(final Long id) {
        final String sql = "SELECT id, name, email, password, role FROM member WHERE id = ?";
        return jdbcTemplate.query(sql, memberRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = "select id, name, email, password, role from member where email = ?";
        return jdbcTemplate.query(sql, memberRowMapper, email)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        final String sql = "select id, name, email, password, role from member where email = ? and password = ?";
        return jdbcTemplate.query(sql, memberRowMapper, email, password)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByEmail(final String email) {
        final String sql = "select exists ( select 1 from member where email = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public void save(final Member member) {
        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole());

        simpleJdbcInsert.executeAndReturnKey(params);
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT id, name, email, password, role FROM member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }
}
