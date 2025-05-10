package roomescape.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.member.dao.MemberDao;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.model.Member;
import roomescape.member.service.fake.FakeMemberDao;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceTest {

    private MemberDao memberDao;

    @BeforeEach
    void initialize() {
        memberDao = new FakeMemberDao();
    }

    @Test
    void 멤버를_추가한다() {
        // Given
        Member member = Member.generateNormalMember("프리", "phree@woowa.com", "password");

        // When
        // Then
        assertThatCode(() -> memberDao.add(member))
                .doesNotThrowAnyException();
    }

    @Test
    void 중복된_이름을_가진_멤버는_추가할_수_없다() {
        // Given
        Member member1 = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        Member member2 = Member.generateNormalMember("프리", "phree2@woowa.com", "password");
        memberDao.add(member1);

        // When
        // Then
        assertThatThrownBy(() -> memberDao.add(member2))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessage("이미 존재하는 이름이다.");
    }

    @Test
    void 중복된_이메일을_가진_멤버는_추가할_수_없다() {
        // Given
        Member member1 = Member.generateNormalMember("프리", "phree@woowa.com", "password");
        Member member2 = Member.generateNormalMember("프리2", "phree@woowa.com", "password");
        memberDao.add(member1);

        // When
        // Then
        assertThatThrownBy(() -> memberDao.add(member2))
                .isInstanceOf(DuplicateMemberException.class)
                .hasMessage("이미 가입된 이메일이다.");
    }
}