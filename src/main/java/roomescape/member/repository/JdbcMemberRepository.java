package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@RequiredArgsConstructor
@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean existsByEmailAndPassword(final String email, final String password) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM members AS m
                    WHERE m.email = ? AND m.password = ?
                    LIMIT 1
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, email, password);
    }

    @Override
    public Optional<String> findNameByEmail(final String email) {
        final String sql = "SELECT name FROM members WHERE email = ?";

        try {
            final String name = jdbcTemplate.queryForObject(sql, String.class, email);
            return Optional.ofNullable(name);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = """
                SELECT id, name, email, password, role
                FROM members
                WHERE email = ?
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Member(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
            ), email));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(final Long memberId) {
        final String sql = """
                SELECT id, name, email, password, role
                FROM members
                WHERE id = ?
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) -> new Member(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    Role.valueOf(rs.getString("role"))
            ), memberId));
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        final String sql = """
                SELECT id, name, email, password, role
                FROM members
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Member(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role"))
        ));
    }
}
