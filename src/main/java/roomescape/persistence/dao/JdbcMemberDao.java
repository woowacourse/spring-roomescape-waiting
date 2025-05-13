package roomescape.persistence.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.business.domain.Member;

@Repository
public class JdbcMemberDao implements MemberDao {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ROLE = "role";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final RowMapper<Member> memberRowMapper =
            (rs, rowNum) -> new Member(
                    rs.getLong(ID),
                    rs.getString(NAME),
                    rs.getString(ROLE),
                    rs.getString(EMAIL),
                    rs.getString(PASSWORD)
            );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Member> findAll() {
        final String sql = """
                SELECT id, name, role, email, password
                FROM member
                """;
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    @Override
    public Optional<Member> findById(final Long memberId) {
        final String sql = """
                SELECT id, name, role, email, password
                FROM member
                WHERE id = ?
                """;
        try {
            final Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, memberId);
            return Optional.of(member);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        final String sql = """
                SELECT id, name, role, email, password
                FROM member
                WHERE email = ? AND password = ?
                """;
        try {
            final Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, email, password);
            return Optional.of(member);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(final Long memberId) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM member
                    WHERE id = ?
                ) AS is_exist
                """;
        final int flag = jdbcTemplate.queryForObject(sql, Integer.class, memberId);
        return flag == 1;
    }
}
