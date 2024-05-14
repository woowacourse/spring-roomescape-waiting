package roomescape.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberEmail;
import roomescape.domain.MemberName;
import roomescape.domain.MemberPassword;
import roomescape.domain.MemberRepository;
import roomescape.service.request.MemberSignUpAppRequest;
import roomescape.service.response.MemberAppResponse;

@Service
public class MemberAuthService {

    private final MemberRepository memberRepository;

    public MemberAuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public MemberAppResponse signUp(MemberSignUpAppRequest request) {
        if (memberRepository.isExistsByEmail(request.email())) {
            throw new IllegalStateException("해당 이메일의 회원이 이미 존재합니다.");
        }

        Member newMember = Member.createUser(
            new MemberName(request.name()),
            new MemberEmail(request.email()),
            new MemberPassword(request.password()));

        Member savedMember = memberRepository.save(newMember);
        return new MemberAppResponse(savedMember.getId(), savedMember.getName().getName(),
            savedMember.getRole().name());
    }

    public MemberAppResponse findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
            .map(member -> new MemberAppResponse(member.getId(), member.getName().getName(),
                member.getRole().name()))
            .orElseThrow(() -> new NoSuchElementException("회원 정보를 찾지 못했습니다. 다시 로그인 해주세요."));
    }

    public List<MemberAppResponse> findAll() {
        return memberRepository.findAll().stream()
            .map(member -> new MemberAppResponse(member.getId(), member.getName().getName(),
                member.getRole().name()))
            .toList();
    }

    public boolean isExistsMemberByEmailAndPassword(String email, String password) {
        if (memberRepository.isExistsByEmailAndPassword(email, password)) {
            return true;
        }
        throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
    }
}
