package roomescape.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.LoginRequest;
import roomescape.dto.MemberPreviewResponse;
import roomescape.dto.MemberReservationResponse;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.util.JwtProvider;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository reservationWaitingRepository;
    private final JwtProvider jwtProvider;

    public MemberService(MemberRepository memberRepository, ReservationWaitingRepository reservationWaitingRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.jwtProvider = jwtProvider;
    }

    public String login(LoginRequest loginRequest) {
        String email = loginRequest.email();
        String password = loginRequest.password();
        Member member = getMemberByEmailAndPassword(email, password);
        return jwtProvider.createToken(member.getId());
    }

    private Member getMemberByEmailAndPassword(String email, String password) {
        return memberRepository.findByEmailAndPassword(email, password)
                .orElseThrow(() -> new ResourceNotFoundException("일치하는 이메일과 비밀번호가 없습니다."));
    }

    public Member getMemberById(long memberId) {
        return memberRepository.findById(memberId).orElseThrow(
                () -> new ResourceNotFoundException("아이디에 해당하는 사용자가 없습니다."));
    }

    public List<MemberPreviewResponse> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(MemberPreviewResponse::from)
                .toList();
    }

    public List<MemberReservationResponse> findReservationsByMemberId(long memberId) {
        Member member = getMemberById(memberId);
        return member.getReservations()
                .stream()
                .map(reservation -> MemberReservationResponse.of(reservation, getReservationWaiting(member, reservation)))
                .toList();
    }

    private ReservationWaitingResponse getReservationWaiting(Member member, Reservation reservation) {
        Optional<ReservationWaiting> waiting = reservationWaitingRepository.findByMemberAndReservation(member, reservation);
        if (waiting.isEmpty()) {
            return null;
        }
        List<ReservationWaiting> reservations = reservationWaitingRepository.findAllByReservation(reservation);
        int rank = reservations.indexOf(waiting.get()) + 1;
        return ReservationWaitingResponse.of(waiting.get(), rank);
    }
}
