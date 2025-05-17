package roomescape.member.service.usecase;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.domain.Account;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.domain.Role;
import roomescape.member.repository.AccountRepository;
import roomescape.member.repository.FakeAccountRepository;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

import static org.assertj.core.api.Assertions.assertThat;

class MemberQueryUseCaseTest {

    private MemberRepository memberRepository;
    private MemberQueryUseCase memberQueryUseCase;
    private AccountRepository accountRepository;
    private AccountQueryUseCase accountQueryUseCase;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        accountRepository = new FakeAccountRepository();
        memberQueryUseCase = new MemberQueryUseCase(memberRepository);
        accountQueryUseCase = new AccountQueryUseCase(accountRepository);
    }

    @Test
    void 저장된_멤버를_불러온다() {
        // given
        final Member member = memberRepository.save(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                )
        );

        // when & then
        assertThat(memberQueryUseCase.get(member.getId()).getEmail())
                .isEqualTo(member.getEmail());
    }

    @Test
    void 저장된_계정을_불러온다() {
        // given
        Member member = memberRepository.save(Member.withoutId(
                MemberName.from("siso"),
                MemberEmail.from("siso@gmail.com"),
                Role.ADMIN
        ));

        final Account account = accountRepository.save(Account.withoutId(
                member, Password.from("1234")
        ));

        final LoginRequest loginRequest = new LoginRequest(
                account.getMember().getEmail().getValue(),
                account.getPassword().getValue()
        );

        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            final Account loadedAccount = accountQueryUseCase.getAccount(loginRequest);
            assertThat(loadedAccount.getMember())
                    .isEqualTo(member);
            assertThat(loadedAccount.getPassword())
                    .isEqualTo(account.getPassword());
        });
    }

    @Test
    void 저장된_모든_회원정보를_불러온다() {
        Member member = Member.withoutId(
                MemberName.from("siso"),
                MemberEmail.from("siso@gmail.com"),
                Role.ADMIN
        );

        final Account account = Account.withoutId(
                member,
                Password.from("1234"));

        final Member savedMember = memberRepository.save(member);
        final Account savedAccount = accountRepository.save(account);

        assertThat(memberQueryUseCase.getAll())
                .contains(MemberConverter.toDto(savedMember));
    }
}
