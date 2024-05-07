package roomescape.member.dao;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRegistrationInfo;

@Repository
public class MemberJdbcDao implements MemberDao {

    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (resultSet, rowNum)
            -> new Member(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("email"),
            resultSet.getString("password"),
            resultSet.getString("role")
    );

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MemberJdbcDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("registration")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Member save(MemberRegistrationInfo memberRegistrationInfo) {
        SqlParameterSource sqlParameterSource = new BeanPropertySqlParameterSource(memberRegistrationInfo);
        long id = jdbcInsert.executeAndReturnKey(sqlParameterSource)
                .longValue();
        return new Member(id, memberRegistrationInfo.name(), memberRegistrationInfo.email(),
                memberRegistrationInfo.password());
    }

    @Override
    public List<Member> findAll() {
        String findAllSql = "SELECT * FROM registration";
        return jdbcTemplate.query(findAllSql, MEMBER_ROW_MAPPER);
    }

    @Override
    public Member findById(long id) {
        String findByIdSql = "SELECT * FROM registration WHERE id = ?";
        return jdbcTemplate.queryForObject(findByIdSql, MEMBER_ROW_MAPPER, id);
    }

    @Override
    public Member findByEmail(String email) {
        String findRegistrationInfSql = "SELECT * FROM registration WHERE email = ?";
        return jdbcTemplate.queryForObject(findRegistrationInfSql, MEMBER_ROW_MAPPER, email);
    }

    @Override
    public void delete(long id) {
        String deleteSql = "DELETE FROM registration WHERE id = ?";
        jdbcTemplate.update(deleteSql, id);
    }

}
