package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.LoginMember;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;

@Repository
public class MemberH2Repository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public MemberH2Repository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<LoginMember> findById(Long id) {
        try {
            LoginMember loginMember = jdbcTemplate.queryForObject(
                    "SELECT * FROM member WHERE id = ?",
                    getMemberRowMapper(),
                    id
            );
            return Optional.ofNullable(loginMember);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<LoginMember> getMemberRowMapper() {
        return (resultSet, rowNum) -> new LoginMember(
                resultSet.getLong("id"),
                new Name(resultSet.getString("name")),
                new Email(resultSet.getString("email")),
                Role.getByDbValue(resultSet.getString("role"))
        );
    }

    @Override
    public Optional<LoginMember> findByEmail(Email email) {
        try {
            LoginMember member = jdbcTemplate.queryForObject(
                    "SELECT * FROM member WHERE email = ?",
                    getMemberRowMapper(),
                    email.email()
            );
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<LoginMember> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM member",
                getMemberRowMapper()
        );
    }

    @Override
    public boolean isCorrectPassword(Email email, Password password) {
        int matchingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM member WHERE email = ? AND password = ?",
                Integer.class,
                email.email(),
                password.password()
        );
        return matchingCount > 0;
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM member WHERE id = ?", id);
    }
}
