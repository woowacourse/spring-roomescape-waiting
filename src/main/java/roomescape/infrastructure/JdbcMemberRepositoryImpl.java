package roomescape.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberEmail;
import roomescape.domain.MemberName;
import roomescape.domain.MemberPassword;
import roomescape.domain.MemberRepository;
import roomescape.domain.MemberRole;

@Repository
public class JdbcMemberRepositoryImpl implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JdbcMemberRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(Objects.requireNonNull(jdbcTemplate.getDataSource()))
            .withTableName("member")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        Map<String, Object> saveSource = Map.ofEntries(
            Map.entry("name", member.getName().getName()),
            Map.entry("email", member.getEmail().getEmail()),
            Map.entry("password", member.getPassword().getPassword()),
            Map.entry("role", member.getRole().name())
        );

        long id = simpleJdbcInsert
            .executeAndReturnKey(saveSource)
            .longValue();

        return new Member(id, member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = "SELECT * FROM member WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getMemberRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isExistsByEmail(String email) {
        String sql = "SELECT EXISTS(SELECT id FROM member WHERE email = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, email);
    }

    @Override
    public boolean isExistsByEmailAndPassword(String email, String password) {
        String sql = "SELECT EXISTS(SELECT id FROM member WHERE email = ? AND password = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, email, password);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = "SELECT * FROM member WHERE email = ? AND password = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getMemberRowMapper(), email, password));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT * FROM member WHERE email = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, getMemberRowMapper(), email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";

        return jdbcTemplate.query(sql, getMemberRowMapper());
    }


    private RowMapper<Member> getMemberRowMapper() {
        return (rs, rowNum) -> new Member(
            rs.getLong("id"),
            new MemberName(rs.getString("name")),
            new MemberEmail(rs.getString("email")),
            new MemberPassword(rs.getString("password")),
            MemberRole.from(rs.getString("role")));
    }
}
