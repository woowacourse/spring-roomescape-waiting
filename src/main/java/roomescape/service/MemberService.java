package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.dto.request.MemberCreateRequest;
import roomescape.dto.response.MemberGetResponse;
import roomescape.exception.DuplicateMemberException;

import java.util.List;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member createUser(MemberCreateRequest memberCreateRequest) {
        String email = memberCreateRequest.email();
        String name = memberCreateRequest.name();
        validateDuplicateUser(email, name);
        Member newMember = Member.generateNormalMember(name, email, memberCreateRequest.password());
        return memberDao.add(newMember);
    }

    private void validateDuplicateUser(String email, String name) {
        if (memberDao.existByEmail(email)) {
            throw new DuplicateMemberException("이미 가입된 이메일이다.");
        }
        if (memberDao.existByName(name)) {
            throw new DuplicateMemberException("이미 존재하는 이름이다.");
        }
    }

    public List<Member> findAllMembers() {
        return memberDao.findAll();
    }
}
