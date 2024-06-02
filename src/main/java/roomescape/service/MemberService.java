package roomescape.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.member.DuplicatedEmailException;
import roomescape.service.dto.request.member.SignupRequest;
import roomescape.service.dto.response.member.MemberResponse;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final ReservationRepository reservationRepository;

    public List<MemberResponse> findAllMember() {
        return memberRepository.findAll()
                .stream()
                .map(member -> new MemberResponse(member.getId(), member.getName()))
                .toList();
    }

    @Transactional
    public long signup(SignupRequest signupRequest) {
        checkDuplicateEmail(signupRequest.email());
        Member savedMember = memberRepository.save(signupRequest.toMember());
        return savedMember.getId();
    }

    private void checkDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicatedEmailException();
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(IllegalArgumentException::new);
        reservationRepository.deleteByMember(findMember);
        memberRepository.delete(findMember);
    }
}
