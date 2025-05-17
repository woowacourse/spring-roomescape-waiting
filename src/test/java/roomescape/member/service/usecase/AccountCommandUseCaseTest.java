package roomescape.member.service.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeAccountRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountCommandUseCaseTest {

    private AccountCommandUseCase accountCommandUseCase;

    @BeforeEach
    void setUp() {
        accountCommandUseCase = new AccountCommandUseCase(
                new FakeAccountRepository()
        );
    }

    @Test
    void 계정이_정상적으로_생성되면_예외가_발생하지_않는다() {
        // when & then
        assertThatNoException().isThrownBy(() -> accountCommandUseCase.create(
                Account.withoutId(
                        Member.withId(
                                1L,
                                MemberName.from("siso"),
                                MemberEmail.from("siso@gmail.com"),
                                Role.ADMIN
                        ),
                        Password.from("1234"))
        ));
    }

    @Test
    void 멤버가_정상적으로_생성된다() {
        // when & then
        assertThat(accountCommandUseCase.create(
                Account.withoutId(
                        Member.withId(
                                1L,
                                MemberName.from("siso"),
                                MemberEmail.from("siso@gmail.com"),
                                Role.ADMIN
                        ),
                        Password.from("1234"))
        ).getId()).isNotNull();
    }

    @Test
    void 이미_저장된_멤버_생성_시_예외가_발생한다() {
        // given
        Account account = Account.withoutId(
                Member.withId(
                        1L,
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                Password.from("1234"));

        accountCommandUseCase.create(account);

        // when & then
        assertThatThrownBy(() -> accountCommandUseCase.create(account))
                .isInstanceOf(AlreadyExistException.class);
    }
}
