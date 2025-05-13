package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@Repository
public class JdbcMemberDao implements MemberDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Member> memberMapper = (resulSet, rowNum) ->
            new Member(
                    resulSet.getLong("id"),
                    resulSet.getString("name"),
                    resulSet.getString("email"),
                    resulSet.getString("password"),
                    MemberRole.valueOf(resulSet.getString("role"))
            );

    public JdbcMemberDao(final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        final String sql = "SELECT * FROM member WHERE email = :email AND password = :password";
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("email", email)
                .addValue("password", password);
        Member member = jdbcTemplate.queryForObject(sql, parameters, memberMapper);
        return Optional.ofNullable(member);
    }

    @Override
    public Optional<Member> findById(long id) {
        final String sql = "SELECT id, name, email, password, role FROM member WHERE id = :id";
        final SqlParameterSource parameters = new MapSqlParameterSource("id", id);
        final Member member = jdbcTemplate.queryForObject(sql, parameters, memberMapper);
        return Optional.ofNullable(member);
    }

    @Override
    public Member save(Member member) {
        final String sql = "INSERT INTO member(name, email, password, role) VALUES(:name, :email, :password, :role)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());
        jdbcTemplate.update(sql, parameters, keyHolder, new String[]{"id"});
        return new Member(keyHolder.getKeyAs(Long.class), member.getName(), member.getEmail(), member.getPassword(),
                member.getRole());
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT id, name, email, password, role FROM member";
        return jdbcTemplate.query(sql, memberMapper);
    }
}
