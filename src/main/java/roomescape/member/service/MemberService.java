package roomescape.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void save(final MemberRequest memberRequest) {
        if (memberRepository.existsByEmail(memberRequest.email())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다. email=" + memberRequest.email());
        }
        memberRepository.save(
                Member.withDefaultRole(memberRequest.name(), memberRequest.email(), memberRequest.password()));
    }

    public List<MemberResponse> findAllMember() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::new)
                .toList();
    }
}
