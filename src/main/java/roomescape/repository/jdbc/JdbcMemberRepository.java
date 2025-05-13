package roomescape.repository.jdbc;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.repository.MemberRepository;

@Repository
public class JdbcMemberRepository implements MemberRepository {

    private static final String DEFAULT_SELECT_SQL = "select id, name, email, password, role from member";

    private static final RowMapper<Member> memberRowMapper = (resultSet, rowNum) -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            Role.valueOf(resultSet.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Member add(Member member) {
        String sql = "insert into member (name, email, password, role) values(?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPassword());
            ps.setString(4, member.getRole().toString());
            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();

        return new Member(
                generatedId,
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
    }

    @Override
    public List<Member> findAll() {
        return jdbcTemplate.query(DEFAULT_SELECT_SQL, memberRowMapper);
    }

    @Override
    public Optional<Member> findById(long id) {
        String sql = DEFAULT_SELECT_SQL + " where id = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, id);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        String sql = DEFAULT_SELECT_SQL + " where email = ? and password = ?";
        try {
            Member member = jdbcTemplate.queryForObject(sql, memberRowMapper, email, password);
            return Optional.of(member);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "select count(id) from member where email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
