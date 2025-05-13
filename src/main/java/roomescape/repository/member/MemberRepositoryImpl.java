package roomescape.repository.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.enums.Role;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert simpleJdbcInsert;

    public MemberRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Optional<Member> findMemberByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member where email=? and password=?";
        Member member = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
            Member foundMember = new Member(resultSet.getLong("id"), resultSet.getString("name"),
                    resultSet.getString("email"), resultSet.getString("password"),
                    Role.from(resultSet.getString("role")));
            return foundMember;
        }, email, password);
        return Optional.of(member);
    }

    @Override
    public Optional<Member> findMemberById(Long id) {
        String sql = "SELECT * FROM member where id=?";
        Member member = jdbcTemplate.queryForObject(sql, (resultSet, rowNum) -> {
            Member foundMember = new Member(resultSet.getLong("id"), resultSet.getString("name"),
                    resultSet.getString("email"), resultSet.getString("password"),
                    Role.from(resultSet.getString("role")));
            return foundMember;
        }, id);
        return Optional.of(member);
    }

    @Override
    public List<Member> findAllMembers() {
        String sql = "SELECT * FROM member";
        List<Member> members = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
                    Member member = new Member(resultSet.getLong("id"), resultSet.getString("name"),
                            resultSet.getString("email"), resultSet.getString("password"),
                            Role.from(resultSet.getString("role")));
                    return member;
                }
        );
        return members;
    }

    @Override
    public Long addMember(Member member) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", member.getName());
        map.put("email", member.getEmail());
        map.put("password", member.getPassword());
        map.put("role", member.getRole());

        Long memberId = simpleJdbcInsert.executeAndReturnKey(map).longValue();
        return memberId;
    }
}
