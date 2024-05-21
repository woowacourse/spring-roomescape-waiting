package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.MemberFixture;

@DataJpaTest
class MemberQueryRepositoryTest {

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @BeforeEach
    void setUp() {
        Member member = MemberFixture.defaultValue();
        memberCommandRepository.save(member);
    }

    @DisplayName("존재하지 않는 회원의 id로 조회하면 예외가 발생한다.")
    @Test
    void getByIdThrowsExceptionWhenDoesNotExists() {
        assertThatCode(() -> memberQueryRepository.getById(1L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_MEMBER);
    }

    @DisplayName("존재하지 않는 회원의 email로 조회하면 예외가 발생한다.")
    @Test
    void getByEmailThrowsExceptionWhenDoesNotExists() {
        assertThatCode(() -> memberQueryRepository.getByEmail(new Email("notExists@email.com")))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_MEMBER);
    }
}
