package roomescape.domain.member.infrastructure.db;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.domain.member.model.Member;
import roomescape.domain.member.model.Role;

@Component
@RequiredArgsConstructor
public class MemberH2Dao implements MemberDao {

    private static final RowMapper<Member> DEFAULT_ROW_MAPPER = (resultSet, rowNum) ->
            Member.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .email(resultSet.getString("email"))
                    .role(Role.valueOf(resultSet.getString("role")))
                    .build();

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Optional<Member> selectByEmailAndPassword(String email, String password) {
        String selectQuery = """
                SELECT id, name, email, role
                FROM member
                WHERE member.email = :email AND member.password = :password
                """;

        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(selectQuery,
                    Map.of("email", email, "password", password),
                    DEFAULT_ROW_MAPPER
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> getAll() {
        String selectQuery = """
                SELECT id, name, email, role
                FROM member
                """;
        return namedParameterJdbcTemplate.query(selectQuery, DEFAULT_ROW_MAPPER);
    }

    @Override
    public boolean existsById(Long memberId) {
        String existsQuery = """
                SELECT EXISTS (
                            SELECT 1
                            FROM member
                            WHERE id = :memberId
                        )
                """;
        Integer result = namedParameterJdbcTemplate.queryForObject(existsQuery, Map.of("memberId", memberId), Integer.class);
        return result != null && result == 1;
    }
}
