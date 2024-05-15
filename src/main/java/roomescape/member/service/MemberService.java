package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.BadRequestException;
import roomescape.member.dao.MemberRepository;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberLoginRequest;
import roomescape.member.dto.MemberProfileInfo;

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
                .orElseThrow(() -> new BadRequestException("등록되지 않은 이메일입니다."));
    }

    public Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("등록되지 않은 회원 ID 입니다."));
    }

}
