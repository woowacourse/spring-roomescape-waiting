package roomescape.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;

@Component
public class ManagerFilter extends RoleCheckFilter {
    public ManagerFilter(MemberDao memberDao, ObjectMapper objectMapper) {
        super(memberDao, objectMapper);
    }

    @Override
    protected boolean hasRequiredRole(Member member) {
        return member.isManager();
    }
}
