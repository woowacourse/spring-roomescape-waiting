package roomescape.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> new Member(
            rs.getLong("id"),
            MemberRole.fromName(rs.getString("role")),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("name"),
            rs.getString("session_id")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(this.jdbcTemplate).withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public boolean existBySessionId(final String sessionId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE session_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, sessionId));
    }
    @Override
    public Member save(final Member member) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("role", member.getRole().getName());
        parameters.put("email", member.getEmail());
        parameters.put("password", member.getPassword());
        parameters.put("name", member.getName());
        parameters.put("session_id", member.getSessionId());
        Number key = jdbcInsert.executeAndReturnKey(parameters);
        return member.createWithId(key.longValue());
    }

    @Override
    public void updateSessionId(final long memberId, final String sessionId) {
        String sql = "UPDATE member SET session_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, sessionId, memberId);
    }

    @Override
    public Optional<Member> findById(final long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        List<Member> members = jdbcTemplate.query(sql, MEMBER_ROW_MAPPER, id);
        if (members.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(members.getFirst());
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        List<Member> members = jdbcTemplate.query(sql, MEMBER_ROW_MAPPER, email);
        if (members.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(members.getFirst());
    }

    @Override
    public boolean existByEmail(final String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE email = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    @Override
    public boolean existByName(final String name) {
        String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE name = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, name));
    }
}
