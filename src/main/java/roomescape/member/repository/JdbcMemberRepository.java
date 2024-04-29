package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> ROW_MAPPER = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.from(resultSet.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("MEMBER")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Member> findAll() {
        String sql = """
                SELECT id, name, email, password, role
                FROM member;
                """;
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT m.id, m.name, m.email, m.password, m.role FROM member AS m WHERE m.id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT m.id, m.name, m.email, m.password, m.role FROM member AS m WHERE m.email = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, ROW_MAPPER, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByEmail(String email) {
        String sql = """
                SELECT EXISTS ( 
                    SELECT 1 
                    FROM member 
                    WHERE email = ?
                );
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public boolean existByName(String name) {
        String sql = """
                SELECT EXISTS ( 
                    SELECT 1 
                    FROM member 
                    WHERE name = ?
                );
                """;
        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource parameters = new BeanPropertySqlParameterSource(member);
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return new Member(id, member);
    }
}
