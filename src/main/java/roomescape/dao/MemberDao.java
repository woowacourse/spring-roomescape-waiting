package roomescape.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.rowmapper.MemberRowMapper;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;

@Repository
public class MemberDao implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final MemberRowMapper memberRowMapper;

    public MemberDao(JdbcTemplate jdbcTemplate, DataSource dataSource, MemberRowMapper memberRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.memberRowMapper = memberRowMapper;
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        List<Member> member = jdbcTemplate.query(sql, memberRowMapper, id);
        return DataAccessUtils.optionalResult(member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = """
                SELECT *
                FROM member
                WHERE email = ?
                """;
        List<Member> member = jdbcTemplate.query(sql, memberRowMapper, email);
        return DataAccessUtils.optionalResult(member);
    }

    @Override
    public Member save(Member member) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", member.getName());
        parameters.put("email", member.getEmail());
        parameters.put("password", member.getPassword());
        parameters.put("role", member.getRole());
        long id = jdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Member(id, member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }
}
