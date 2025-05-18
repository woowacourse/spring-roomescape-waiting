package roomescape.member.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.domain.MemberFixture.NOT_SAVED_MEMBER_1;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.fixture.config.TestConfig;
import roomescape.member.domain.Member;

@DataJpaTest
@Import(TestConfig.class)
@DisplayNameGeneration(ReplaceUnderscores.class)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void ID_값에_해당하는_테마를_반환한다() {
        // given
        final Member saved = memberRepository.save(NOT_SAVED_MEMBER_1());

        // when
        final Member found = memberRepository.getByIdOrThrow(saved.getId());

        // then
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(saved.getId()),
                () -> assertThat(found.getName()).isEqualTo(saved.getName()),
                () -> assertThat(found.getEmail()).isEqualTo(saved.getEmail()),
                () -> assertThat(found.getPassword()).isEqualTo(saved.getPassword())
        );
    }

    @Test
    void ID_값에_해당하는_테마가_없으면_예외가_발생한다() {
        // given
        final Long id = 99L;

        // when & then
        assertThatThrownBy(() -> memberRepository.getByIdOrThrow(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("해당 회원을 찾을 수 없습니다.");
    }
}
