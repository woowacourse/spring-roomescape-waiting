package roomescape.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;
import roomescape.persistence.query.CreateMemberQuery;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    public static final RowMapper<Member> userRowMapper = (rs, rowNum) ->
            new Member(rs.getLong("id"),
                    rs.getString("name"),
                    MemberRole.of(rs.getString("role")),
                    rs.getString("email"),
                    rs.getString("password")
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        String sql = "SELECT id, role, name, email, password FROM member WHERE email = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, userRowMapper, email);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(final Long id) {
        String sql = "SELECT id, role, name, email, password FROM member WHERE id = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Long create(final CreateMemberQuery createMemberQuery) {
        String sql = "INSERT INTO member (name, role, email, password) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, createMemberQuery.name());
            ps.setString(2, createMemberQuery.role().name());
            ps.setString(3, createMemberQuery.email());
            ps.setString(4, createMemberQuery.password());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public List<Member> findAll() {
        return jdbcTemplate.query("SELECT id, role, name, email, password FROM member", userRowMapper);
    }
}
