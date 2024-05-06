package roomescape.infrastructure.role.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import roomescape.domain.role.MemberRole;
import roomescape.domain.role.Role;

public class RoleRowMapper {

    private RoleRowMapper() {
    }

    public static MemberRole mapRow(ResultSet rs) throws SQLException {
        return new MemberRole(
                rs.getLong("member_id"),
                rs.getString("name"),
                Role.from(rs.getString("role"))
        );
    }
}
