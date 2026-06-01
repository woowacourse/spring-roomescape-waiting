package roomescape.member.repository;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Member> memberRowMapper = (resultSet, rowMapper) ->
        Member.load(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
        );

    public JdbcMemberRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
            .withTableName("member")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource params = new MapSqlParameterSource()
            .addValue("name", member.getName())
            .addValue("password", member.getPassword().getValue())
            .addValue("role", member.getRole().name());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Member.load(id, member.getName(), member.getPassword().getValue(), member.getRole());
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = """
            SELECT id, name, password, role 
            FROM member
            WHERE id = :id
            """;
        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id),
                    memberRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByName(String name) {
        String sql = """
            SELECT id, name, password, role 
            FROM member
            WHERE name = :name
            """;
        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("name", name),
                    memberRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
