package roomescape.service.dto.member;

import roomescape.controller.helper.LoginMember;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;

public class MemberResponse {

    private final long id;
    private final String name;

    public MemberResponse(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public MemberResponse(Member member) {
        this(member.getId(), member.getName());
    }

    public MemberResponse(LoginMember member) {
        this(member.getId(), member.getName());
    }

    public MemberResponse(Reservation reservation) {
        this(reservation.memberId(), reservation.memberName());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
