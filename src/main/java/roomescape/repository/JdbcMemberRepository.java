package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberName;
import roomescape.domain.MemberRepository;
import roomescape.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> MEMBER_MAPPER = (resultSet, row) ->
            new Member(
                    resultSet.getLong("id"),
                    new MemberName(resultSet.getString("name")),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    Role.of(resultSet.getString("role"))
            );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(member);
        Number id = jdbcInsert.executeAndReturnKey(parameterSource);
        return new Member(id.longValue(), member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, MEMBER_MAPPER, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, MEMBER_MAPPER, email));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAllMembers() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, MEMBER_MAPPER);
    }
}
