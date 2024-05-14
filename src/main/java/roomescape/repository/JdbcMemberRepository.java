package roomescape.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Members;
import roomescape.domain.Role;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("email"),
            rs.getString("password")
    );
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Members findAll() {
        List<Member> members = jdbcTemplate.query(
                "SELECT id, name, role, email, password FROM MEMBER",
                MEMBER_ROW_MAPPER);
        return new Members(members);
    }

    public Optional<Member> findById(long id) {
        try {
            Member findMember = jdbcTemplate.queryForObject(
                    "SELECT id, name, role, email, password FROM MEMBER WHERE id = ?",
                    MEMBER_ROW_MAPPER,
                    id
            );
            return Optional.of(findMember);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        try {
            Member findMember = jdbcTemplate.queryForObject(
                    "SELECT id, name, role, email, password FROM MEMBER WHERE email = ?",
                    MEMBER_ROW_MAPPER,
                    email
            );
            return Optional.of(findMember);
        } catch (DataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Member save(Member member) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((con -> {
            PreparedStatement preparedStatement = con.prepareStatement(
                    "INSERT INTO MEMBER (NAME, ROLE, EMAIL, PASSWORD) VALUES (?,?,?,?)",
                    new String[]{"id"}
            );
            preparedStatement.setString(1, member.getName());
            preparedStatement.setString(2, member.getRole().name());
            preparedStatement.setString(3, member.getEmail());
            preparedStatement.setString(4, member.getPassword());
            return preparedStatement;
        }), keyHolder);

        long savedId = keyHolder.getKey().longValue();
        Optional<Member> byId = findById(savedId);
        return byId.get();
    }
}
