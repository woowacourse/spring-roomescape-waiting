package roomescape.dao.jdbc;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@Repository
public class MemberJdbcDao implements MemberDao {
    private static final RowMapper<Member> ROW_MAPPER = (rs, rowNum) -> new Member(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"),
            MemberRole.valueOf(rs.getString("role")),
            rs.getObject("store_id", Long.class)
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public MemberJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("members")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "email", "password", "role", "store_id");
    }

    @Override
    public Member insert(Member member) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", member.getName())
                .addValue("email", member.getEmail())
                .addValue("password", member.getPassword())
                .addValue("role", member.getRole().name())
                .addValue("store_id", member.getStoreId());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Member(id, member.getName(), member.getEmail(), member.getPassword(),
                member.getRole(), member.getStoreId());
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        try {
            Member member = jdbcTemplate.queryForObject(
                    "SELECT id, name, email, password, role, store_id FROM members WHERE email = ?",
                    ROW_MAPPER, email);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findById(Long id) {
        try {
            Member member = jdbcTemplate.queryForObject(
                    "SELECT id, name, email, password, role, store_id FROM members WHERE id = ?",
                    ROW_MAPPER, id);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
