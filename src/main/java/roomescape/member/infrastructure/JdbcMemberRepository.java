package roomescape.member.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> ROW_MAPPER = (resultSet, rowNum) -> {
        String role = resultSet.getString("role");
        Role memberRole = Role.findRole(role);
        return Member.createWithId(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                memberRole
        );
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcMemberRepository(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Long save(Member member) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", member.getName());
        parameters.put("email", member.getEmail());
        parameters.put("password", member.getPassword());
        parameters.put("role", member.getRole());

        return jdbcInsert.executeAndReturnKey(parameters).longValue();
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByEmail(String email) {
        String sql = "SELECT EXISTS (SELECT 1 FROM member WHERE email = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member WHERE email = ? and password = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, email, password));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }
}
