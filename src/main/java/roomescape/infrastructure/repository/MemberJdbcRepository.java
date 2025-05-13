package roomescape.infrastructure.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberJdbcRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<Member> memberRapper = (resultSet, rowNum) -> Member.create(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            Role.valueOf(resultSet.getString("role")),
            resultSet.getString("email"),
            resultSet.getString("password")
    );

    public MemberJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * from member WHERE email = ?";
        return jdbcTemplate.query(sql, memberRapper, email)
                .stream()
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * from member";
        return jdbcTemplate.query(sql, memberRapper);
    }

    @Override
    public Member save(Member member) {
        String name = member.getName();
        Role role = member.getRole();
        String email = member.getEmail();
        String password = member.getPassword();

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("role", role)
                .addValue("email", email)
                .addValue("password", password);

        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return Member.create(id, name, role, email, password);
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * from member WHERE id = ?";
        return jdbcTemplate.query(sql, memberRapper, id)
                .stream()
                .findFirst();
    }
}
