package roomescape.repository.member.legacy;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.repository.member.MemberRepository;

public class JdbcMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcMemberRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Member> memberRowMapper = (resultSet, rowNumber) -> {
        long id = resultSet.getLong("id");
        String username = resultSet.getString("username");
        String password = resultSet.getString("password");
        String name = resultSet.getString("name");
        Role role = Role.valueOf(resultSet.getString("role"));

        return new Member(id, username, password, name, role);
    };

    @Override
    public long add(Member member) {
        String sql = "insert into member (username,password,name,role) values(?,?,?,?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, member.getUsername());
            ps.setString(2, member.getPassword());
            ps.setString(3, member.getName());
            ps.setString(4, member.getRole().name());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public Optional<Member> findById(long id) {
        try {
            String sql = "select id,username,password,name,role from member where id=?";
            return Optional.of(jdbcTemplate.queryForObject(sql, memberRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        try {
            String sql = "select id,username,password,name,role from member where username=?";
            return Optional.of(jdbcTemplate.queryForObject(sql, memberRowMapper, username));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existByUsername(String username) {
        String sql = "select exists (select 1 from member where username = ?)";
        return jdbcTemplate.queryForObject(sql, Boolean.class, username);
    }

    @Override
    public List<Member> findAll() {
        String sql = "select id,username,password,name,role from member";
        return jdbcTemplate.query(sql, memberRowMapper);
    }
}
