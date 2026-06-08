package roomescape.member.adapter.out.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.member.application.port.out.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

@Repository
@RequiredArgsConstructor
public class JdbcMemberRepository implements MemberRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("password"),
                    Role.valueOf(resultSet.getString("role"))
            );


    @Override
    public Optional<Member> findByName(String name) {
        String sql = "SELECT * FROM member WHERE name = :name";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", name);

        return jdbcTemplate.query(sql, params, memberRowMapper)
                .stream()
                .findFirst();
    }
}
