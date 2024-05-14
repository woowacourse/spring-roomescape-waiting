package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.Name;
import roomescape.domain.member.Role;

import java.util.List;
import java.util.Optional;

class MemberDaoTest extends DaoTest {

    @Autowired
    private MemberDao memberDao;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberDao.save(MEMBER_BROWN());
    }

    @Test
    @DisplayName("사용자를 저장한다.")
    void saveMember() {
        // given
        final Member member = new Member(new Name("미르"), "mir@email.com", "1234", Role.MEMBER);

        // when
        final Member savedMember = memberDao.save(member);

        // then
        assertThat(savedMember.getId()).isNotNull();
    }

    @Test
    @DisplayName("Id에 해당하는 사용자를 조회한다.")
    void findMemberById() {
        // when
        final Optional<Member> actual = memberDao.findById(member.getId());

        // then
        assertThat(actual).hasValue(member);
    }

    @Test
    @DisplayName("Id에 해당하는 사용자가 없으면 빈 옵셔널을 조회한다.")
    void returnEmptyOptionalWhenFindMemberByNotExistingId() {
        // given
        final Long notExistingId = 4L;

        // when
        final Optional<Member> actual = memberDao.findById(notExistingId);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("email에 해당하는 사용자를 조회한다.")
    void findMemberByEmail() {
        // when
        final Optional<Member> actual = memberDao.findByEmail(member.getEmail());

        // then
        assertThat(actual).hasValue(member);
    }

    @Test
    @DisplayName("email에 해당하는 사용자가 없으면 빈 옵셔널을 조회한다.")
    void returnEmptyOptionalWhenFindMemberByNotExistingEmail() {
        // given
        final String notExistingEmail = "odd@email.com";

        // when
        final Optional<Member> actual = memberDao.findByEmail(notExistingEmail);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("사용자 전체 목록을 조회한다.")
    void findAllMembers() {
        // when
        final List<Member> actual = memberDao.findAll();

        // then
        assertThat(actual).hasSize(3);
    }
}
