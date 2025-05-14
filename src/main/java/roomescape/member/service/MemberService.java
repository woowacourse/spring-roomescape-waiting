package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberDao;

@Service
public class MemberService {

    private final MemberDao memberDao;

    public MemberService(final MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member findMemberByEmailAndPassword(final String email, final String password) {
        return memberDao.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new EntityNotFoundException("이메일 또는 패스워드가 잘못 되었습니다."));
    }

    public List<MemberResponse> findAll() {
        return memberDao.findAll()
                .stream()
                .map(MemberResponse::fromEntity)
                .toList();
    }

    public void create(final @Valid MemberCreateRequest request) {
        final Member member = new Member(
                request.name(),
                request.email(),
                request.password(),
                Role.MEMBER
        );
        memberDao.save(member);
    }
}
