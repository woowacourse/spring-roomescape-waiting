package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import roomescape.exception.AuthorizationLoginFailException;
import roomescape.member.domain.Member;
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
        return memberRepository.findAll()
                .stream()
                .map(member -> new MemberProfileInfo(member.getId(), member.getName(), member.getEmail()))
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
}
