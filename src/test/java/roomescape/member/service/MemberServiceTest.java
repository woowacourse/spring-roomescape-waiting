package roomescape.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.exception.model.MemberNotFoundException;
import roomescape.member.repository.FakeMemberRepository;

public class MemberServiceTest {

    private final MemberService memberService;

    public MemberServiceTest() {
        this.memberService = new MemberService(new FakeMemberRepository());
    }

    @Test
    @DisplayName("존재하는 멤버가 없을 경우 에러가 발생한다.")
    void notExistMemberReservation() {
        Throwable notExistMember = assertThrows(MemberNotFoundException.class,
                () -> memberService.findMember(100));
        assertEquals(notExistMember.getMessage(), new MemberNotFoundException().getMessage());
    }

}
