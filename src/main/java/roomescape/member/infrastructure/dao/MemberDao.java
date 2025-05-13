package roomescape.member.infrastructure.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.application.repository.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.presentation.dto.SignUpRequest;

@Repository
public class MemberDao implements MemberRepository {
    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum) ->
            new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("email"),
                    resultSet.getString("password"),
                    resultSet.getString("name"),
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
    public Member insert(final SignUpRequest signUpRequest) {
        Map<String, Object> params = new HashMap<>();
        params.put("email", signUpRequest.getEmail());
        params.put("password", signUpRequest.getPassword());
        params.put("name", signUpRequest.getName());
        params.put("role", Role.USER);

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Member(id, signUpRequest.getEmail(), signUpRequest.getPassword(), signUpRequest.getName(),
                Role.USER);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "select id, email, password, name, role from member where email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql, MEMBER_ROW_MAPPER, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "select id, email, password, name, role from member where id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql, MEMBER_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAllMembers() {
        String sql = " select id, email, password, name, role from member";
        return jdbcTemplate.query(sql, MEMBER_ROW_MAPPER);
    }
}
