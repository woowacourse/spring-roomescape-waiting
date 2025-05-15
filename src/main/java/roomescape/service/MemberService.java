package roomescape.service;

import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.dto.MemberRegisterRequest;
import roomescape.dto.MemberRegisterResponse;
import roomescape.dto.MemberResponse;
import roomescape.repository.MemberRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberRegisterResponse addMember(final MemberRegisterRequest request) {
        validateDuplicateEmail(request.email());
        validateDuplicateName(request.name());
        final Member newMember = Member.builder()
                .email(request.email())
                .name(request.name())
                .password(request.password())
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
        return memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("[ERROR] 사용자가 존재하지 않습니다."));
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
    private String encode(final String rawPassword) {
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }
}
