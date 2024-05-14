package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.exception.BadRequestException;

@Repository
public class MemberRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final RowMapper<Member> memberRowMapper = (rs, rowNum) -> new Member(
            rs.getLong("member_id"),
            rs.getString("member_name"),
            rs.getString("member_email"),
            rs.getString("member_password"),
            Role.findBy(rs.getString("member_role")));

    public MemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
    }

    public List<Member> findAll() {
        String selectSQL = """
                SELECT
                m.id AS member_id,
                m.name AS member_name,
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role
                FROM member AS m;
                """;
        return jdbcTemplate.query(selectSQL, memberRowMapper);
    }

    public Optional<Member> findByEmailAndPassword(String email, String password) {
        if (email == null) {
            throw new BadRequestException("이메일이 빈값일 수 없습니다.");
        }

        if (password == null) {
            throw new BadRequestException("비밀번호가 빈값일 수 없습니다.");
        }

        String selectSQL = """
               SELECT
                   m.ID as member_id,
                   m.NAME as member_name,
                   m.EMAIL as member_email,
                   m.PASSWORD as member_password,
                   m.role AS member_role
               FROM member AS m
               WHERE m.EMAIL = ? and m.PASSWORD = ?
               """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(selectSQL, memberRowMapper, email, password));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Optional<Member> findById(Long id) {
        if (id == null) {
            throw new BadRequestException("사용자가 빈값일 수 없습니다.");
        }

        String selectSQL = """
               SELECT
                   m.ID as member_id,
                   m.NAME as member_name,
                   m.EMAIL as member_email,
                   m.PASSWORD as member_password,
                   m.role AS member_role
               FROM member AS m
               WHERE m.ID = ?
               """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(selectSQL, memberRowMapper, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    public Member save(Member member) {
        if (member == null) {
            throw new BadRequestException("사용자가 빈값일 수 없습니다.");
        }

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("id", member.getId())
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return member.setId(id);
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }
        return jdbcTemplate.update("DELETE FROM member WHERE id = ?", id) > 0;
    }
}
