package roomescape.member;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.auth.PasswordEncoder;
import roomescape.auth.dto.LoginMember;
import roomescape.exception.custom.reason.member.MemberEmailConflictException;
import roomescape.exception.custom.reason.member.MemberNotFoundException;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepositoryFacade memberRepositoryFacade;
    private final PasswordEncoder passwordEncoder;

    public void createMember(final MemberRequest request) {
        validateDuplicationEmail(request);

        final String encodedPassword = passwordEncoder.encode(request.password());

        final Member notSavedMember = new Member(request.email(), encodedPassword, request.name(), MemberRole.MEMBER);
        memberRepositoryFacade.save(notSavedMember);
    }

    public List<MemberReservationResponse> readAllReservationsByMember(final LoginMember loginMember) {
        final Member member = memberRepositoryFacade.findByEmail(loginMember.email())
                .orElseThrow(MemberNotFoundException::new);

        return member.getReservations().stream()
                .map(MemberReservationResponse::of)
                .toList();
    }

    public List<MemberResponse> readAllMember() {
        return memberRepositoryFacade.findAll().stream()
                .map(MemberResponse::from)
                .toList();
    }

    private void validateDuplicationEmail(final MemberRequest request) {
        if (memberRepositoryFacade.existsByEmail(request.email())) {
            throw new MemberEmailConflictException();
        }
    }
}
