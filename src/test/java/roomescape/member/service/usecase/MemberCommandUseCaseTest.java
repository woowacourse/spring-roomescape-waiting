package roomescape.member.service.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Role;
import roomescape.member.repository.FakeMemberRepository;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class MemberCommandUseCaseTest {

    private MemberCommandUseCase memberCommandUseCase;

    @BeforeEach
    void setUp() {
        memberCommandUseCase = new MemberCommandUseCase(
                new FakeMemberRepository()
        );
    }

    @Test
    void 멤버가_정상적으로_생성되면_예외가_발생하지_않는다() {
        // when & then
        assertThatNoException().isThrownBy(() -> memberCommandUseCase.create(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                )
        ));
    }

    @Test
    void 멤버가_정상적으로_생성된다() {
        // when & then
        assertThat(memberCommandUseCase.create(
                Member.withoutId(
                        MemberName.from("siso"),
                        MemberEmail.from("siso@gmail.com"),
                        Role.ADMIN
                )
        ).getId()).isNotNull();
    }

    @Test
    void 이미_저장된_멤버_생성_시_예외가_발생한다() {
        // given
        Member member = Member.withoutId(
                MemberName.from("siso"),
                MemberEmail.from("siso@gmail.com"),
                Role.ADMIN
        );

        memberCommandUseCase.create(member);

        // when & then
        assertThatThrownBy(() -> memberCommandUseCase.create(member))
                .isInstanceOf(AlreadyExistException.class);
    }
}
