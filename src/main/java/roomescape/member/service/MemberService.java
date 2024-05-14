package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.repository.MemberRepository;
import roomescape.member.dto.MemberResponse;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<MemberResponse> readMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
