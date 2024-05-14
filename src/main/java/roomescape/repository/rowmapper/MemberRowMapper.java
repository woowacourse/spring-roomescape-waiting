package roomescape.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Role;

@Component
public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        return new Member(id, name, email, password, role);
    }
}
