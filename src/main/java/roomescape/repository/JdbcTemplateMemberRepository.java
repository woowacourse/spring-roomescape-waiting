package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.repository.rowmapper.MemberRowMapper;

@Repository
public class JdbcTemplateMemberRepository implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final MemberRowMapper memberRowMapper;

    public JdbcTemplateMemberRepository(JdbcTemplate jdbcTemplate, MemberRowMapper memberRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.memberRowMapper = memberRowMapper;
    }

    @Override
    public Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword) {
        String query = "SELECT id, name, email, password, role FROM member WHERE email = ? AND password = ?";
        return jdbcTemplate.query(query, memberRowMapper, email, encryptedPassword)
                .stream()
                .findAny();
    }

    @Override
    public Optional<Member> findById(long id) {
        return jdbcTemplate.query("SELECT id, name, email, password, role FROM member WHERE id = ?", memberRowMapper,
                        id)
                .stream()
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return jdbcTemplate.query("SELECT id, name, email, password, role FROM member", memberRowMapper);
    }
}
