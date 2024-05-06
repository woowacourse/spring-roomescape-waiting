package roomescape.infrastructure.member;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.infrastructure.member.rowmapper.MemberRowMapper;

@Repository
public class JdbcMemberRepository implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingColumns("name", "email", "password")
                .usingGeneratedKeyColumns("id");
    }

    public Member save(Member member) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword());
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return member.withId(id);
    }

    @Override
    public List<Member> findAll() {
        String sql = "select id, name, email, password from member";
        return jdbcTemplate.query(sql, (rs, rowNum) -> MemberRowMapper.mapRow(rs));
    }

    public Optional<Member> findById(long id) {
        String sql = "select id, name, email, password from member where id = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> MemberRowMapper.mapRow(rs), id);
            return Optional.ofNullable(member);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "select exists(select 1 from member where email = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, email);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "select id, name, email, password from member where email = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> MemberRowMapper.mapRow(rs), email);
            return Optional.ofNullable(member);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }
}
