package roomescape.dao;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.mapper.MemberMapper;
import roomescape.domain.user.Member;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberDao {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final MemberMapper rowMapper;

    public MemberDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource, final MemberMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = rowMapper;
    }

    public Member create(final Member member) {
        final SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("password", member.getPassword())
                .addValue("email", member.getEmail())
                .addValue("role", member.getRole());

        final long id = jdbcInsert.executeAndReturnKey(params)
                .longValue();
        return Member.fromMember(id, member.getName(), member.getEmail(), member.getPassword());
    }
    public boolean isExistByEmail(final String email) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM member WHERE email = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    public Optional<Member> findByEmail(final String email) {
        final String sql = "SELECT id, name, email, password,role FROM member WHERE email = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, email));
        } catch (final EmptyResultDataAccessException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    public Optional<Member> findById(final long id) {
        final String sql = "SELECT id, name, email, password,role FROM member WHERE id=?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (final EmptyResultDataAccessException | NullPointerException exception) {
            return Optional.empty();
        }
    }

    public List<Member> getAllMembers() {
        final String sql = "SELECT id, name, email, password,role FROM member";
        return jdbcTemplate.query(sql, rowMapper);
    }



}
