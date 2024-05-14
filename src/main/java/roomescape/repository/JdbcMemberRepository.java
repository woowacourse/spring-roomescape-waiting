package roomescape.repository;

import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberInfo;

@Repository
public class JdbcMemberRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Member> rowMapper;

    public JdbcMemberRepository(DataSource dataSource, RowMapper<Member> rowMapper) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = rowMapper;
    }

    public Member insertMember(Member member) {
        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name());
        long memberId = jdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return findMemberById(memberId).get();
    }

    public boolean isMemberExistsByEmail(String email) {
        String sql = "SELECT 1 FROM member WHERE email = :email";
        SqlParameterSource paramMap = new MapSqlParameterSource()
                .addValue("email", email);
        List<Integer> result = jdbcTemplate.queryForList(sql, paramMap, Integer.class);
        return !result.isEmpty();
    }

    public Optional<Member> findMemberByEmail(String email) {
        String sql = "SELECT id, email, password, name, role FROM member WHERE email = :email";
        SqlParameterSource paramMap = new MapSqlParameterSource().addValue("email", email);
        List<Member> members = jdbcTemplate.query(sql, paramMap, rowMapper);
        return Optional.ofNullable(members.isEmpty() ? null : members.get(0));
    }

    public Optional<Member> findMemberById(long id) {
        String sql = "SELECT id, email, password, name, role FROM member WHERE id = :id";
        SqlParameterSource paramMap = new MapSqlParameterSource().addValue("id", id);
        List<Member> members = jdbcTemplate.query(sql, paramMap, rowMapper);
        return Optional.ofNullable(members.isEmpty() ? null : members.get(0));
    }

    public List<MemberInfo> findAllMemberNames() {
        String sql = "SELECT id, name FROM member";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            long id = rs.getLong("id");
            String name = rs.getString("name");
            return new MemberInfo(id, name);
        });
    }
}
