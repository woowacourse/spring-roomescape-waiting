package roomescape.member.application.service;

import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.jwt.TokenProvider;
import roomescape.member.application.repository.MemberRepository;
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

    public String createToken(TokenRequest tokenRequest) {
        Member member = memberRepository.findByEmail(tokenRequest.getEmail())
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));

        if (!member.getPassword().equals(tokenRequest.getPassword())) {
            throw new IllegalArgumentException("패스워드가 맞지 않습니다.");
        }

        return tokenProvider.createToken(member);
    }

    public MemberResponse findByToken(String token) {
        Long id = tokenProvider.getInfo(token).getId();
        return new MemberResponse(findById(id));
    }

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("유저 정보를 찾을 수 없습니다."));
    }

    @Transactional
    public SignUpResponse signUp(SignUpRequest signUpRequest) {
        if (memberRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        return new SignUpResponse(memberRepository.insert(signUpRequest).getId());
    }

    public List<MemberResponse> getMembers() {
        return memberRepository.findAllMembers().stream()
                .map(MemberResponse::new)
                .toList();
    }
}
