package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.acceptance.PreInsertedData.CUSTOMER_1;

class MemberRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("이메일와 비밀번호에 해당하는 사용자를 찾는다.")
    @Nested
    class findByEmailAndPassword {

        @DisplayName("일치하는 사용자가 있다면, 그 사용자를 반환한다.")
        @Test
        void findByEmailAndPassword_givenCorrectInfo() {
            String email = CUSTOMER_1.getEmail();
            String password = CUSTOMER_1.getPassword();

            Optional<Member> foundMember = memberRepository.findByEmailAndPassword(email, password);

            assertThat(foundMember).contains(CUSTOMER_1);
        }

        @DisplayName("일치하는 사용자가 없다면, 빈 값을 반환한다.")
        @ParameterizedTest(name = "{0}")
        @MethodSource("notMatchEmailAndPasswordProvider")
        void findByEmailAndPassword_givenWrongInfo(String name, String email, String password) {
            Optional<Member> foundMember = memberRepository.findByEmailAndPassword(email, password);

            assertThat(foundMember).isNotPresent();
        }

        static Stream<Arguments> notMatchEmailAndPasswordProvider() {
            return Stream.of(
                    Arguments.of("이메일만 잘못된 경우", "wrongEmail", CUSTOMER_1.getPassword()),
                    Arguments.of("비밀번호만 잘못된 경우", CUSTOMER_1.getEmail(), "wrongPassword"),
                    Arguments.of("둘 다 잘못된 경우", "wrongEmail", "wrongPassword")
            );
        }
    }
}
