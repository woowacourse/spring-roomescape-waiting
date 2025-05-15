package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.repository.jpa.MemberJpaRepository;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("name"),
            rs.getString("session_id"),
            MemberRole.fromName(rs.getString("role"))
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberRepositoryImpl(final MemberJpaRepository memberJpaRepository, final JdbcTemplate jdbcTemplate) {
        this.memberJpaRepository = memberJpaRepository;
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
        return memberJpaRepository.save(member);
    }

    @Override
    public void updateSessionId(final long memberId, final String sessionId) {
        String sql = "UPDATE member SET session_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, sessionId, memberId);
    }

    @Override
    public Optional<Member> findById(final long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existByEmail(final String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existByName(final String name) {
        return memberJpaRepository.existsByName(name);
    }
}
