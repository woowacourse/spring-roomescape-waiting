package roomescape.member.dao;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.Member;
import roomescape.member.MemberDao;
import roomescape.member.MemberRole;
import roomescape.store.Store;

@Repository
public class MemberJdbcDao implements MemberDao {
    private static final String BASE_SELECT = """
            SELECT m.id AS member_id, m.name AS member_name, m.email, m.password, m.role,
                   s.id AS store_id, s.name AS store_name
            FROM members m
            LEFT JOIN stores s ON m.store_id = s.id
            """;

    private static final RowMapper<Member> ROW_MAPPER = (rs, rowNum) -> {
        Long storeId = rs.getObject("store_id", Long.class);
        Store store = storeId == null ? null : new Store(storeId, rs.getString("store_name"));
        return new Member(
                rs.getLong("member_id"),
                rs.getString("member_name"),
                rs.getString("email"),
                rs.getString("password"),
                MemberRole.valueOf(rs.getString("role")),
                store
        );
    };

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
                member.getRole(), member.getStore());
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        try {
            Member member = jdbcTemplate.queryForObject(
                    BASE_SELECT + " WHERE m.email = ?",
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
                    BASE_SELECT + " WHERE m.id = ?",
                    ROW_MAPPER, id);
            return Optional.ofNullable(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
