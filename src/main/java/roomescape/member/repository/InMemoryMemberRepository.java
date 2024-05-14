package roomescape.member.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InMemoryMemberRepository implements MemberRepository {

    private final NamedParameterJdbcTemplate template;

    public InMemoryMemberRepository(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT * FROM member";

        return template.query(sql, itemRowMapper());
    }

    @Override
    public Optional<Member> findById(final Long memberId) {
        final String sql = "SELECT * FROM member WHERE id = :id";

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", memberId);
            final Member member = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(member);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Member> itemRowMapper() {
        return ((rs, rowNum) -> Member.createMemberWithId(
                rs.getLong("id"),
                MemberRole.of(rs.getString("role")),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("email")
        ));
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = "SELECT * FROM member WHERE email = :email";

        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("email", email);
            final Member member = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(member);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Member save(final Member member) {
        final String sql = """
            INSERT INTO member (role, password, name, email)
            VALUES (:role, :password, :name, :email)
        """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("role", member.getRole().name())
                .addValue("password", member.getPassword().value())
                .addValue("name", member.getName().value())
                .addValue("email", member.getEmail().value());
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        final long savedMemberId = Objects.requireNonNull(keyHolder.getKey()).longValue();

        return member.initializeIndex(savedMemberId);
    }

    @Override
    public boolean existByEmail(final String email) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE email = :email)";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("email", email);

        return Boolean.TRUE.equals(template.queryForObject(sql, param, Boolean.class));
    }
}
