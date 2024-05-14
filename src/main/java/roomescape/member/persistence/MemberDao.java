package roomescape.member.persistence;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.Role;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class MemberDao implements MemberRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberDao(DataSource source) {
        this.jdbcTemplate = new JdbcTemplate(source);
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(Member member) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(member);
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Member(Objects.requireNonNull(id), member);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM member WHERE email = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToMember, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        try {
            String sql = "SELECT * FROM member WHERE id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToMember, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT * FROM member";
        return jdbcTemplate.query(sql, this::mapRowToMember);
    }

    private Member mapRowToMember(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Member(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                Role.valueOf(resultSet.getString("role"))
        );
    }
}
