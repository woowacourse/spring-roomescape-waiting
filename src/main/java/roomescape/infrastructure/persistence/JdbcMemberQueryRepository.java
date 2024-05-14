package roomescape.infrastructure.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberQueryRepository;
import roomescape.infrastructure.persistence.rowmapper.MemberRowMapper;

@Repository
public class JdbcMemberQueryRepository implements MemberQueryRepository {
    private static final String SQL = """
            select 
                id as member_id, 
                name as member_name, 
                email as member_email, 
                password as member_password,
                role as member_role
            from 
                member
            """;

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Member> findById(Long id) {
        String sql = SQL + " where id = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, MemberRowMapper::mapRow, id);
            return Optional.of(Objects.requireNonNull(member));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        String sql = SQL + " where email = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, MemberRowMapper::mapRow, email);
            return Optional.of(Objects.requireNonNull(member));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Member> findAll() {
        return jdbcTemplate.query(SQL, MemberRowMapper::mapRow);
    }
}
