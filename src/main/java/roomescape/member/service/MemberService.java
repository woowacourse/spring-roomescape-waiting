package roomescape.member.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Service;
import roomescape.member.controller.dto.request.SignupRequest;
import roomescape.member.controller.dto.response.MemberResponse;
import roomescape.member.controller.dto.response.SignupResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member findByEmail(final Email email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException(
                        "[ERROR] (email : " + email + ") 에 대한 사용자가 존재하지 않습니다.")
                );
    }

    public SignupResponse save(final SignupRequest signupRequest) {
        Member member = memberRepository.save(signupRequest.toEntity());
        return new SignupResponse(member.getNameValue(), member.getEmail(), member.getPassword());
    }


    public List<MemberResponse> getAll() {
        return StreamSupport.stream(memberRepository.findAll().spliterator(), false)
                .map(MemberResponse::from)
                .toList();
    }
}
