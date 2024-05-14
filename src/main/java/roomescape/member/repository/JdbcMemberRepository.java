package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepository(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .usingGeneratedKeyColumns("id")
                .withTableName("member");
    }

    private final RowMapper<Member> memberRowMapper =
            (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    MemberRole.getMemberRole(resultSet.getString("role")),
                    resultSet.getString("email"),
                    resultSet.getString("password")
            );


    @Override
    public Member save(final Member member) {
        SqlParameterSource source = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("role", member.getMemberRole().name())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword());

        long id = simpleJdbcInsert.executeAndReturnKey(source).longValue();
        return Member.of(id, member);
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        String sql = """
                select * 
                from member
                where email = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, memberRowMapper, email));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(final Long id) {
        String sql = """
                select * 
                from member
                where id = ?
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, memberRowMapper, id));
        } catch (EmptyResultDataAccessException emptyResultDataAccessException) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = """
                select * 
                from member
                """;
        return jdbcTemplate.query(sql, memberRowMapper);
    }
}
