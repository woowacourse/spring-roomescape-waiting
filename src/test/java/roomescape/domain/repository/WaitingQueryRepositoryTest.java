package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.MemberFixture;
import roomescape.ThemeFixture;
import roomescape.TimeFixture;
import roomescape.WaitingFixture;
import roomescape.application.ServiceTest;
import roomescape.domain.Member;
import roomescape.domain.Theme;
import roomescape.domain.Time;
import roomescape.domain.Waiting;
import roomescape.domain.dto.WaitingWithRank;
import roomescape.exception.RoomescapeErrorCode;
import roomescape.exception.RoomescapeException;

@ServiceTest
class WaitingQueryRepositoryTest {

    @Autowired
    private WaitingQueryRepository waitingQueryRepository;

    @Autowired
    private WaitingCommandRepository waitingCommandRepository;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private TimeCommandRepository timeCommandRepository;

    @Autowired
    private ThemeCommandRepository themeCommandRepository;

    @DisplayName("회원의 id로 예약 대기와 대기 순번을 조회한다.")
    @Test
    void findWaitingWithRankByMemberIdTest() {
        Member member = memberCommandRepository.save(MemberFixture.defaultValue());
        LocalDate date = LocalDate.now();
        Time time = timeCommandRepository.save(TimeFixture.defaultValue());
        Theme theme = themeCommandRepository.save(ThemeFixture.defaultValue());
        waitingCommandRepository.save(WaitingFixture.of(member, date, time, theme));

        List<WaitingWithRank> waitingWithRank = waitingQueryRepository.findWaitingWithRankByMemberId(member.getId());

        assertThat(waitingWithRank.size()).isOne();
    }

    @DisplayName("존재하지 않는 예약 대기 id로 조회시 예외가 발생한다.")
    @Test
    void getByIdTest() {
        assertThatCode(() -> waitingQueryRepository.getById(100L))
                .isInstanceOf(RoomescapeException.class)
                .extracting("errorCode")
                .isEqualTo(RoomescapeErrorCode.NOT_FOUND_WAITING);
    }
}
