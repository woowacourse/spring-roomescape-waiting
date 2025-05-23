package roomescape.member.application;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.jwt.TokenProvider;
import roomescape.member.domain.Member;
import roomescape.member.presentation.dto.MemberResponse;
import roomescape.member.presentation.dto.SignUpRequest;
import roomescape.member.presentation.dto.SignUpResponse;
import roomescape.member.presentation.dto.TokenRequest;

@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    public MemberService(MemberRepository memberRepository, TokenProvider tokenProvider) {
        this.memberRepository = memberRepository;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Member member = request.toUserMember();
        return new SignUpResponse(memberRepository.save(member).getId());
    }

    public String createToken(TokenRequest request) {
        Member member = getMemberByEmail(request.getEmail());

        if (member.isNotMatchingPassword(request.getPassword())) {
            throw new IllegalArgumentException("패스워드가 맞지 않습니다.");
        }
        return tokenProvider.createToken(member);
    }

    public MemberResponse findByToken(String token) {
        Long id = tokenProvider.getInfo(token).getId();
        return new MemberResponse(getMemberById(id));
    }

    public Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }

    public List<MemberResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::new)
                .toList();
    }

    private Member getMemberByEmail(final String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }
}
