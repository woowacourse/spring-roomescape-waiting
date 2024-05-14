package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.MembersResponse;

import java.util.List;

@Service
public class MemberService {
    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public MembersResponse findAllMembers() {
        List<MemberResponse> response = memberDao.findAll().stream()
                .map(MemberResponse::fromEntity)
                .toList();

        return new MembersResponse(response);
    }

    public Member findMemberById(final Long memberId) {
        return memberDao.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        String.format("회원(Member) 정보가 존재하지 않습니다. [memberId: %d]", memberId)));
    }

    public Member findMemberByEmailAndPassword(final String email, final String password) {
        return memberDao.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_NOT_FOUND,
                        String.format("회원(Member) 정보가 존재하지 않습니다. [email: %s, password: %s]", email, password)));
    }
}
