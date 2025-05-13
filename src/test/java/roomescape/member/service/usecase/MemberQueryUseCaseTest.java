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
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberConverter;

import static org.assertj.core.api.Assertions.*;

class MemberQueryUseCaseTest {

    private MemberRepository memberRepository;
    private MemberQueryUseCase memberQueryUseCase;

    @BeforeEach
    void setUp() {
        memberRepository = new FakeMemberRepository();
        memberQueryUseCase = new MemberQueryUseCase(memberRepository);
    }

    @Test
    void 저장된_멤버를_불러온다() {
        // given
        final Account account = Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                Password.from("1234"));

        final Member member = memberRepository.save(account);

        // when & then
        assertThat(memberQueryUseCase.get(member.getId()).getEmail())
                .isEqualTo(account.getMember().getEmail());
    }

    @Test
    void 저장된_계정을_불러온다() {
        // given
        final Account account = Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                Password.from("1234"));

        final Member member = memberRepository.save(account);

        final LoginRequest loginRequest = new LoginRequest(
                account.getMember().getEmail().getValue(),
                account.getPassword().getValue()
        );

        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            final Account loadedAccount = memberQueryUseCase.getAccount(loginRequest);
            assertThat(loadedAccount.getMember())
                    .isEqualTo(member);
            assertThat(loadedAccount.getPassword())
                    .isEqualTo(account.getPassword());
        });
    }

    @Test
    void 저장된_모든_회원정보를_불러온다() {
        final Account account = Account.of(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                ),
                Password.from("1234"));

        final Member member = memberRepository.save(account);

        assertThat(memberQueryUseCase.getAll())
                .contains(MemberConverter.toDto(member));
    }
}
