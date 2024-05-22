package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.WaitingWithRank;

/*
 * 예약 대기 초기 데이터
 * {ID=1, DATE='2024-04-30', TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 * {ID=2, DATE=내일일자, TIME_ID=1, THEME_ID=1, MEMBER_ID=2, STATUS=WAITING}
 */
@DataJpaTest
class WaitingRepositoryTest {
    @Autowired
    private WaitingRepository waitingRepository;

    @ParameterizedTest
    @CsvSource(value = {"2024-04-30,1,2,true", "2024-05-01,1,2,false", "2024-04-30,2,2,false", "2024-04-30,1,1,false"})
    @DisplayName("예약 날짜, 시간Id, 회원Id를 통해 예약 대기여부를 확인할 수 있다")
    void given_when_isExist_then_getExistWaiting(LocalDate date, Long timeId, Long memberId, boolean expected) {
        //when
        boolean result = waitingRepository.existsByDateAndTimeIdAndMemberId(date, timeId, memberId);

        //then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("특정 회원의 순번을 포함한 예약 대기 정보를 조회한다.")
    void given_memberId_when_find_then_getWaitingsWithRank() {
        //given, when
        List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(2L);

        //then
        assertAll(
                () -> assertThat(waitings).hasSize(2),
                () -> assertThat(waitings.get(0).getRank()).isEqualTo(1)
        );
    }
}
