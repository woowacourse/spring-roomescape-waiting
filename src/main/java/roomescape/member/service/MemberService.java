package roomescape.member.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.response.FindMembersResponse;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.repository.ReservationRepository;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(final MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<FindMembersResponse> getMembers() {
        return memberRepository.findAll().stream()
                .map(FindMembersResponse::of)
                .toList();
    }
}
