package roomescape.infrastructure.persistance;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final static RowMapper<Member> MEMBER_ROW_MAPPER = ((rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("name"),
            new Email(rs.getString("email")),
            rs.getString("password"),
            Role.valueOf(rs.getString("role"))
    ));

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public JdbcMemberRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = """
                SELECT id, name, email, password, role
                FROM member
                WHERE id = :id
                """;
        try {
            Member member = namedParameterJdbcTemplate.queryForObject(sql, Map.of("id", id), MEMBER_ROW_MAPPER);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = """
                SELECT id, name, email, password, role
                FROM member
                WHERE email = :email
                """;
        try {
            Member member = namedParameterJdbcTemplate.queryForObject(sql, Map.of("email", email), MEMBER_ROW_MAPPER);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = """
                SELECT id, name, email, password, role
                FROM member
                """;
        return namedParameterJdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }
}
