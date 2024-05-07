package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.member.dao.MemberJdbcDao;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;

@Service
public class MemberService {

    private final MemberJdbcDao memberJdbcDao;

    public MemberService(MemberJdbcDao memberJdbcDao) {
        this.memberJdbcDao = memberJdbcDao;
    }

    public List<MemberProfileInfo> findAllMembers() {
        return memberJdbcDao.findAll()
                .stream()
                .map(member -> new MemberProfileInfo(member.getId(), member.getName(), member.getEmail()))
                .toList();
    }

    public Member findMember(MemberLoginRequest memberLoginRequest) {
        Member member = memberJdbcDao.findByEmail(memberLoginRequest.email());
        validateLoginRequest(member);
        return member;
    }

    private void validateLoginRequest(Member member) {
        if (member == null || member.getEmail() == null || member.getPassword() == null) {
            throw new BadRequestException("등록되지 않은 회원입니다.");
        }
    }

    public Member findMemberById(Long id) {
        return memberJdbcDao.findById(id);
    }

}
