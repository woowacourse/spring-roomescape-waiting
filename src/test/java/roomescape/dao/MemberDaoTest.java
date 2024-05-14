package roomescape.dao;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Member;

@SpringBootTest
class MemberDaoTest {
    @Autowired
    private MemberDao memberDao;

    private Member dummyMember;

    @BeforeEach
    void setUp() {
        dummyMember = new Member("name", "email", "password");
    }

    @AfterEach
    void tearDown() {
        memberDao.deleteAll();
    }

    @Test
    @DisplayName("사용자 정보를 저장한다")
    void save_ShouldStorePersistence() {
        // given
        dummyMember = new Member("name", "email", "password");

        // when
        Member savedMember = memberDao.save(dummyMember);

        // then
        Assertions.assertThat(memberDao.findById(savedMember.getId())).isPresent();
    }

    @Test
    @DisplayName("영속성에 사용자 있으면 참을 반환한다")
    void existsByEmailAndPassword_ShouldGetPersistence() {
        // given
        dummyMember = new Member("name", "email", "password");
        memberDao.save(dummyMember);

        // when & then
        Assertions.assertThat(memberDao.findByEmailAndPassword(dummyMember.getEmail(), dummyMember.getPassword()))
                .isPresent();
    }

    @Test
    @DisplayName("영속성에 사용자 없으면 거짓을 반환한다")
    void existsByEmailAndPassword_ShouldReturnNull_WhenTargetDoesNotExist() {
        // when & then
        Assertions.assertThat(memberDao.findByEmailAndPassword(dummyMember.getEmail(), dummyMember.getPassword()))
                .isEmpty();
    }

    @Test
    @DisplayName("저장 된 모든 사용자들을 찾아낸다.")
    void findAll_ShouldReturnAllPersistence() {
        // given
        memberDao.save(dummyMember);
        memberDao.save(dummyMember);

        memberDao.save(dummyMember);
        memberDao.save(dummyMember);
        memberDao.save(dummyMember);

        // when
        List<Member> members = memberDao.findAll();

        // then
        Assertions.assertThat(members).hasSize(5);

    }

    @Test
    @DisplayName("이메일이 존재하는지 확인 할 수 잇다 -존재")
    void existsByEmail_ShouldCheckExistenceOfMember() {
        // given
        memberDao.save(new Member("name", "email", "aa"));

        // when
        boolean result = memberDao.existsByEmail("email");

        // then
        Assertions.assertThat(result).isTrue();

    }

    @Test
    @DisplayName("이메일이 존재하는지 확인 할 수 있다 - 존재X")
    void existsByEmail_ShouldCheckDoesNotExistenceOfMember() {
        // given
        memberDao.save(new Member("name", "email", "aa"));

        // when
        boolean result = memberDao.existsByEmail("email2");

        // then
        Assertions.assertThat(result).isFalse();

    }

    @Test
    @DisplayName("회원을 삭제할 수 있다")
    void delete_ShouldRemovePersistence() {
        // given
        Member savedMember = memberDao.save(dummyMember);

        // when
        memberDao.delete(savedMember);

        // then
        Assertions.assertThat(memberDao.findAll()).isEmpty();
    }

}
