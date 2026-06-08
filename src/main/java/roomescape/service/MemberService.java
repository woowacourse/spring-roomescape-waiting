package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import roomescape.controller.dto.LoginMemberResponse;
import roomescape.domain.Role;
import roomescape.repository.MemberDao;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public List<LoginMemberResponse> findUsers() {
        return memberDao.findByRole(Role.USER)
                .stream()
                .map(LoginMemberResponse::from)
                .toList();
    }
}
