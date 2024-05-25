package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.exception.AuthorizationLoginFailException;
import roomescape.exception.ConflictException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberProfileInfo> findAllMembers() {
        return memberRepository.findAllByRole(MemberRole.USER)
                .stream()
                .map(MemberProfileInfo::from)
                .toList();
    }

    public Member findMember(MemberLoginRequest memberLoginRequest) {
        return memberRepository.findByEmail(memberLoginRequest.email())
                .orElseThrow(AuthorizationLoginFailException::new);
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(AuthorizationLoginFailException::new);
    }

    public MemberProfileInfo createMember(MemberCreateRequest request) {
        memberRepository.findByEmail(request.email())
                .ifPresent(member -> {
                    throw new ConflictException("해당 이메일을 사용하는 사용자가 존재합니다.");
                });

        Member member = request.createMember();
        member = memberRepository.save(member);

        return MemberProfileInfo.from(member);
    }
}
