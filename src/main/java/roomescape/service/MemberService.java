package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.ReservationRepository;
import roomescape.dto.request.LogInRequest;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.service.exception.ResourceNotFoundException;
import roomescape.util.JwtProvider;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, ReservationRepository reservationRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
        this.jwtProvider = jwtProvider;
    }

    public String logIn(LogInRequest logInRequest) {
        String email = logInRequest.email();
        String password = logInRequest.password();

        Member member = findMemberByEmailAndPassword(email, password);

        return jwtProvider.createToken(member);
    }

    private Member findMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("일치하는 이메일과 비밀번호가 없습니다."));
    }

    public Member findValidatedSiteUserById(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException("아이디에 해당하는 사용자가 없습니다."));
    }

    public List<MemberPreviewResponse> getAllMemberPreview() {
        return memberRepository.findAll().stream()
                .map(MemberPreviewResponse::from)
                .toList();
    }

    public List<MemberReservationResponse> getMyReservations(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
