package roomescape.member.service;

import org.springframework.stereotype.Service;
import roomescape.member.dto.SaveMemberRequest;
import roomescape.member.encoder.PasswordEncoder;
import roomescape.member.model.Member;
import roomescape.member.repository.MemberRepository;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(final MemberRepository memberRepository, final PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Member> getMembers() {
        return memberRepository.findAll();
    }

    public Member saveMember(final SaveMemberRequest request) {
        validateEmailDuplicate(request.email());
        validatePlainPassword(request.password());

        final Member member = request.toMember(passwordEncoder.encode(request.password()));
        return memberRepository.save(member);
    }

    private void validateEmailDuplicate(final String email) {
        if (memberRepository.existByEmail(email)) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
    }

    private void validatePlainPassword(final String password) {
        final int minimumEnableLength = 10;
        final int maximumEnableLength = 30;

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("회원 비밀번호로 공백을 입력할 수 없습니다.");
        }

        if (password.length() < minimumEnableLength || password.length() > maximumEnableLength) {
            throw new IllegalArgumentException("회원 비밀번호 길이는 " + minimumEnableLength + "이상 "
                    + maximumEnableLength + "이하여만 합니다.");
        }
    }
}
