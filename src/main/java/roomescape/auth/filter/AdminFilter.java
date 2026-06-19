package roomescape.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import roomescape.member.Member;
import roomescape.member.MemberDao;

@Component
public class AdminFilter extends RoleCheckFilter {
    public AdminFilter(MemberDao memberDao, ObjectMapper objectMapper) {
        super(memberDao, objectMapper);
    }

    @Override
    protected boolean hasRequiredRole(Member member) {
        return member.isAdmin();
    }
}
