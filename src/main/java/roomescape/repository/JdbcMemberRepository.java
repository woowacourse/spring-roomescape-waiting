package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Member> rowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String name = rs.getString("name");
        String role = rs.getString("role");

        return new Member(id, email, password, name, Role.valueOf(role));
    };

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("name", member.getName())
                .addValue("role", member.getRole().name());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new Member(
                id,
                member.getEmail(),
                member.getPassword(),
                member.getName(),
                member.getRole()
        );
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";

        try {
            Member member = jdbcTemplate.queryForObject(sql, rowMapper, email);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE email = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }
}
