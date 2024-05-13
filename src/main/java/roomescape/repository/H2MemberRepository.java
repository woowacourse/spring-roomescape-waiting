package roomescape.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.service.exception.MemberNotFoundException;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class H2MemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public H2MemberRepository(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("MEMBER")
                .usingGeneratedKeyColumns("ID");
    }

    @Override
    public List<Member> findAll() {
        final String sql = "SELECT ID, NAME, EMAIL, PASSWORD, ROLE FROM MEMBER";

        return jdbcTemplate.query(sql, this::mapRowMember);
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        final String sql = "SELECT ID, NAME, EMAIL, PASSWORD, ROLE FROM MEMBER WHERE EMAIL = ?";

        return jdbcTemplate.query(sql, this::mapRowMember, email)
                .stream()
                .findAny();
    }

    @Override
    public Member fetchByEmail(final String email) {
        return findByEmail(email).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
    }

    @Override
    public Optional<Member> findById(final long id) {
        final String sql = "SELECT ID, NAME, EMAIL, PASSWORD, ROLE FROM MEMBER WHERE ID = ?";

        return jdbcTemplate.query(sql, this::mapRowMember, id)
                .stream()
                .findAny();
    }

    @Override
    public Member fetchById(final long id) {
        return findById(id).orElseThrow(() -> new MemberNotFoundException("존재하지 않는 회원입니다."));
    }

    @Override
    public Member save(final Member member) {
        final SqlParameterSource params = new BeanPropertySqlParameterSource(member);
        final long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();

        return new Member(id, member.getName(), member.getEmail(), member.getPassword(), member.getRole());
    }

    @Override
    public void delete(final String email) {
        final String sql = "DELETE FROM MEMBER WHERE EMAIL = ?";

        jdbcTemplate.update(sql, email);
    }

    private Member mapRowMember(final ResultSet rs, final int rowNum) throws SQLException {
        return new Member(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("EMAIL"),
                rs.getString("PASSWORD"),
                Role.findByName(rs.getString("ROLE"))
        );
    }
}
