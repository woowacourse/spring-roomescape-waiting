package roomescape.member;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public void createMember(final MemberRequest request) {
        validateDuplicationEmail(request);

        final Member notSavedMember = new Member(request.email(), request.password(), request.name(), MemberRole.MEMBER);
        memberRepository.save(notSavedMember);
    }

    public List<MemberReservationResponse> readAllReservationsByMember(final LoginMember loginMember){
        final Member member = memberRepository.findByEmail(loginMember.email())
                .orElseThrow(() -> new MemberNotFoundException());

        return member.getReservations().stream()
                .map(MemberReservationResponse::of)
                .toList();
    }

    public List<MemberResponse> readAllMember() {
        return memberRepository.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    private void validateDuplicationEmail(final MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberEmailConflictException();
        }
    }
}
