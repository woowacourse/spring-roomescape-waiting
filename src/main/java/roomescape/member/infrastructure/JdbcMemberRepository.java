package roomescape.member.infrastructure;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.auth.domain.AuthRole;
import roomescape.exception.resource.InCorrectResultSizeException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberCommandRepository;
import roomescape.member.domain.MemberQueryRepository;

@Repository
@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberCommandRepository, MemberQueryRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) ->
            new Member(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("password"),
                    AuthRole.from(rs.getString("role"))
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Member member) {
        // TODO: exists를 통해 확인 후 영속시키기
        final String sql = """
                INSERT INTO members (name, email, password, role)
                VALUES (?, ?, ?, ?)
                """;

        final KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPassword());
            ps.setString(4, member.getRoleName());
            return ps;
        }, keyHolder);

        return Optional.of(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new IllegalStateException("회원 추가에 실패했습니다."));
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = "DELETE FROM members WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = """
                SELECT
                    id,
                    name,
                    email,
                    password,
                    role
                FROM members
                WHERE email = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("회원이 여러명 존재합니다.");
        }
    }

    @Override
    public Optional<Member> findById(final Long id) {
        final String sql = """
                SELECT
                    id,
                    name,
                    email,
                    password,
                    role
                FROM members
                WHERE id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("회원이 여러명 존재합니다.");
        }
    }

    @Override
    public Member getById(Long id) {
        final String sql = """
                SELECT
                    id,
                    name,
                    email,
                    password,
                    role
                FROM members
                WHERE id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("회원이 존재하지 않습니다.");
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("회원이 여러명 존재합니다.");
        }
    }

    @Override
    public List<Member> findAll() {
        final String sql = """
                SELECT
                    id,
                    name,
                    email,
                    password,
                    role
                FROM members
                """;

        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }
}
