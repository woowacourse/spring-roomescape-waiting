package roomescape.member.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roomescape.common.template.AbstractDaoTemplate;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Repository
public class MemberDao extends AbstractDaoTemplate<Member> {

    private static final String TABLE_NAME = "member";
    private static final String BASE_SELECT_SQL = "SELECT id, name, email, password, role FROM member";

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );

    @Autowired
    public MemberDao(final NamedParameterJdbcTemplate jdbcTemplate, final DataSource dataSource) {
        super(jdbcTemplate, TABLE_NAME, dataSource);
    }

    @Override
    protected RowMapper<Member> rowMapper() {
        return memberRowMapper;
    }

    public Member save(final Member member) {
        Map<String, Object> params = Map.of(
                "name", member.getName(),
                "email", member.getEmail(),
                "password", member.getPassword(),
                "role", member.getRole()
        );
        long memberId = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Member(
                memberId,
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                Role.valueOf(member.getRole())
        );
    }

    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        String sql = BASE_SELECT_SQL + " WHERE email = :email AND password = :password";
        return executeQueryForObject(sql, Map.of("email", email, "password", password));
    }

    public Optional<Member> findById(final Long id) {
        String sql = BASE_SELECT_SQL + " WHERE id = :id";
        return executeQueryForObject(sql, Map.of("id", id));
    }

    public List<Member> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, memberRowMapper);
    }
}
