package roomescape.unit.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.entity.Member;
import roomescape.global.ReservationStatus;
import roomescape.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Sql(scripts = "/sql/member-repository-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void id를_기반으로_예약을_fetch_하여_member를_찾는다() {
        //given
        //when
        Optional<Member> member = memberRepository.findFetchById(1L);

        //then
        assertAll(
                () -> assertThat(member).isPresent(),
                () -> assertThat(member.get().getReservations()).hasSize(1)
        );
    }

    @Test
    void 예약_대기자_중_다음_예약_대상자를_찾는다() {
        //given
        Member expected = memberRepository.findById(2L).get();

        //when
        List<Member> actual = memberRepository.findNextReserveMember(LocalDate.of(2025, 7, 1), 1L, 1L,
                ReservationStatus.WAIT, PageRequest.of(0, 1));

        //then
        assertAll(
                () -> assertThat(actual).hasSize(1),
                () -> assertThat(actual.getFirst()).isEqualTo(expected)
        );
    }
}
