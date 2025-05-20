package roomescape.member.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRole;
import roomescape.member.domain.Password;
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
                Member.builder()
                        .name(memberRequest.name())
                        .email(memberRequest.email())
                        .password(Password.createForMember(memberRequest.password()))
                        .role(MemberRole.MEMBER)
                        .build()
        );
    }

    public List<MemberResponse> findAllMember() {
        return memberRepository.findAll()
                .stream()
                .map(MemberResponse::new)
                .toList();
    }
}
