package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.WaitingWithRank;

import java.util.List;

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
                () -> assertThat(waitings).hasSize(3),
                () -> assertThat(waitings.get(0).getWaiting().getDate()).isEqualTo("2024-05-17"),
                () -> assertThat(waitings.get(2).getWaiting().getDate()).isEqualTo("2024-05-19")
        );
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
