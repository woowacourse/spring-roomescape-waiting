package roomescape.service.member;

import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.response.MemberRegisterResponse;
import roomescape.dto.response.MemberResponse;
import roomescape.global.PasswordEncoder;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberRegisterResponse addMember(final MemberRegisterRequest request) {
        validateDuplicateEmail(request.email());
        validateDuplicateName(request.name());
        final Member newMember = Member.builder()
                .email(request.email())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .role(MemberRole.USER)
                .build();
        return MemberRegisterResponse.from(memberRepository.save(newMember));
    }

    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    public Member getMemberById(final long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 사용자가 존재하지 않습니다."));
    }

    public Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 사용자가 존재하지 않습니다."));
    }

    private void validateDuplicateEmail(final String email) {
        if (memberRepository.existByEmail(email)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 이메일 입니다.");
        }
    }

    private void validateDuplicateName(final String name) {
        if (memberRepository.existByName(name)) {
            throw new IllegalArgumentException("[ERROR] 이미 존재하는 이름 입니다.");
        }
    }
}
