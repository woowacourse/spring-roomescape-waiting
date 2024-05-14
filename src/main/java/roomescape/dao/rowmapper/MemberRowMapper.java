package roomescape.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;

@Component
public class MemberRowMapper implements RowMapper<Member> {
    @Override
    public Member mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Member(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                MemberRole.findByName(resultSet.getString("role"))
        );
    }

    public Member mapJoinedRow(ResultSet resultSet) throws SQLException {
        return new Member(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("member_email"),
                resultSet.getString("member_password"),
                MemberRole.findByName(resultSet.getString("member_role"))
        );
    }
}
