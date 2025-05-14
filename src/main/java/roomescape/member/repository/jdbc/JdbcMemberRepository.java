package roomescape.member.repository.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.repository.MemberRepository;

@Repository
public class JdbcMemberRepository implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("member")
            .usingGeneratedKeyColumns("id");
    }


    @Override
    public Member save(Member member) {
        Map<String, Object> parameters = Map.ofEntries(
            Map.entry("name", member.getName()),
            Map.entry("email", member.getEmail()),
            Map.entry("member_role", member.getRole().toString()),
            Map.entry("password", member.getPassword())
        );

        Long generatedKey = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Member.generateWithPrimaryKey(member, generatedKey);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String query = "SELECT id, name, email, member_role, password FROM member WHERE email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query,
                (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    MemberRole.valueOf(resultSet.getString("member_role")),
                    resultSet.getString("password")
                ), email));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        String query = "SELECT id, name, email, member_role, password FROM member WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query,
                (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    MemberRole.valueOf(resultSet.getString("member_role")),
                    resultSet.getString("password")
                ), id));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String query = "SELECT id, name, email, member_role, password FROM member";
        return jdbcTemplate.query(
            query,
            (resultSet, rowNum) -> new Member(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                MemberRole.valueOf(resultSet.getString("member_role")),
                resultSet.getString("password")
            ));
    }

    @Override
    public Optional<Member> findByName(String name) {
        String query = "SELECT id, name, email, member_role, password FROM member WHERE name = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query,
                (resultSet, rowNum) -> new Member(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    MemberRole.valueOf(resultSet.getString("member_role")),
                    resultSet.getString("password")
                ), name));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }
}
