package roomescape.dao.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.user.*;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MemberMapper implements RowMapper<Member> {

    @Override
    public Member mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        return new Member(
                rs.getLong("id"),
                new Name(rs.getString("name")),
                new Email(rs.getString("email")),
                new Password(rs.getString("password")),
                Role.from(rs.getString("role"))
        );
    }
}
