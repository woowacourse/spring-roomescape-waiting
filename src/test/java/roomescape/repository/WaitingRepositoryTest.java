package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.Waiting;
import roomescape.domain.WaitingWithRank;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static roomescape.fixture.fixture.*;
import static roomescape.fixture.fixture.USER_MEMBER;

@DataJpaTest
class WaitingRepositoryTest {

    @Autowired
    WaitingRepository waitingRepository;

    @DisplayName("회원의 예약 대기 목록을 날짜 순으로 확인할 수 있다")
    @Test
    void findWaitingsWithRankByMemberIdByDateAsc() {
        //given, when
        List<WaitingWithRank> waitings = waitingRepository
                .findWaitingsWithRankByMemberIdByDateAsc(ADMIN_MEMBER);

        //then
        assertAll(
                () -> assertThat(waitings).hasSize(4),
                () -> assertThat(waitings.get(0).getWaiting().getDate()).isEqualTo("2024-05-17"),
                () -> assertThat(waitings.get(3).getWaiting().getDate()).isEqualTo("2024-05-19")
        );
    }

    @DisplayName("해당 날짜와 시간대와 테마에 존재하는 예약 대기 중 가장 첫 번째 예약 대기를 가지고 온다.")
    @Test
    void findFirstByDateAndTimeAndTheme() {
        //given, when
        Optional<Waiting> waiting = waitingRepository
                .findFirstByDateAndTimeAndTheme(FROM_DATE, TIME_ONE, THEME_ONE);

        //then
        assertThat(waiting.get().getMember().getName()).isEqualTo(USER_MEMBER.getName());
    }

    @DisplayName("해당 date와 theme와 time과 member에 해당하는 예약 대기가 존재하면 true를 반환한다.")
    @Test
    void existsByDateAndTimeAndThemeAndMember_isTrue() {
        //given, when
        boolean isReservationExists_true = waitingRepository
                .existsByDateAndTimeAndThemeAndMember(FROM_DATE, TIME_ONE, THEME_ONE, USER_MEMBER);

        //then
        assertThat(isReservationExists_true).isTrue();
    }

    @DisplayName("해당 date와 theme와 time과 member에 해당하는 예약 대기가 존재하지 않으면 false를 반환한다.")
    @Test
    void existsByDateAndTimeAndThemeAndMember_isFalse() {
        //given, when
        boolean isReservationExists_false = waitingRepository
                .existsByDateAndTimeAndThemeAndMember(FROM_DATE, TIME_TWO, THEME_TWO, ADMIN_MEMBER);

        //then
        assertThat(isReservationExists_false).isFalse();
    }
}
