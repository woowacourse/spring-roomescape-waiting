package roomescape.infrastructure.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.repository.MemberRepository;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> Member.of(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            Optional.ofNullable(rs.getString("role")).map(Role::valueOf).orElse(null)
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT id, name, email, password, role FROM member";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }

    @Override
    public Long save(Member member) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> params = Map.of(
                "name", member.getName(),
                "email", member.getEmail(),
                "password", member.getPassword()
        );

        Number key = simpleJdbcInsert.executeAndReturnKey(params);
        return key.longValue();
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT id, name, email, password, role FROM member WHERE id = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, id);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findBy(String email, String password) {
        String sql = "SELECT id, name, email, password, role FROM member WHERE email = ? AND password = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, MEMBER_ROW_MAPPER, email, password);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
