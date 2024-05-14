package roomescape.member.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberDao implements MemberRepository {

    private static final RowMapper<Member> rowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public MemberDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        Long id = simpleJdbcInsert.executeAndReturnKey(new BeanPropertySqlParameterSource(member))
                .longValue();
        return new Member(id, member);
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return findBy("id", id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return findBy("email", email);
    }

    private Optional<Member> findBy(String column, Object value) {
        try {
            String sql = String.format("SELECT * FROM member WHERE %s = ?", column);
            Member member = jdbcTemplate.queryForObject(sql, rowMapper, value);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }
}
