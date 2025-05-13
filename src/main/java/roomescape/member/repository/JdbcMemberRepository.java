package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;

@Repository
@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Member> rowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            RoleType.valueOf(resultSet.getString("role"))
    );

    @Override
    public Member save(Member member) {
        String query = "INSERT INTO member(name, email, password, role) VALUES (:name, :email, :password, :role)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(query, params, keyHolder);

        return new Member(
                keyHolder.getKey().longValue(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }

    @Override
    public List<Member> findAll() {
        String query = """
                SELECT
                    m.id,
                    m.name,
                    m.email,
                    m.password,
                    m.role
                FROM member as m
                """;

        return jdbcTemplate.query(query, rowMapper);
    }

    @Override
    public Optional<Member> findById(Long id) {
        String query = """
                SELECT
                    m.id,
                    m.name,
                    m.email,
                    m.password,
                    m.role
                FROM member as m
                WHERE m.id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        try {
            Member member = jdbcTemplate.queryForObject(query, params, rowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String query = """
                SELECT
                    m.id,
                    m.name,
                    m.email,
                    m.password,
                    m.role
                FROM member as m
                WHERE m.email = :email
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("email", email);

        try {
            Member member = jdbcTemplate.queryForObject(query, params, rowMapper);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String query = "DELETE FROM member WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        int updated = jdbcTemplate.update(query, params);

        return updated > 0;
    }
}
