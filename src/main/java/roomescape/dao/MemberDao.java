package roomescape.dao;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.domain.repository.MemberRepository;

@Repository
public class MemberDao implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.of(resultSet.getString("role"))
    );

    public MemberDao(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        List<Member> findMembers = jdbcTemplate.query(sql, memberRowMapper, id);
        return DataAccessUtils.optionalResult(findMembers);
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member WHERE email = ? AND password = ?";
        List<Member> findMembers = jdbcTemplate.query(sql, memberRowMapper, email, password);
        return DataAccessUtils.optionalResult(findMembers);
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT 1 FROM member WHERE email = ?)";
        return Objects.requireNonNull(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    @Override
    public Member save(Member member) {
        Map<String, String> sqlParams = Map.of("name", member.getName(),
                "email", member.getEmail(),
                "password", member.getPassword(),
                "role", member.getRoleAsString());
        Long id = jdbcInsert.executeAndReturnKey(sqlParams)
                .longValue();
        return new Member(id, member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public void delete(Member member) {
        String sql = "DELETE FROM member WHERE id = ?";
        jdbcTemplate.update(sql, member.getId());
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM member";
        jdbcTemplate.update(sql);
    }
}
