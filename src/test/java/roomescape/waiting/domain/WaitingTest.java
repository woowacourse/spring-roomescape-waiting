package roomescape.waiting.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.fixture.TestFixture;
import roomescape.member.domain.Member;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

import org.junit.jupiter.api.Test;

public class WaitingTest {
    private final Member member = TestFixture.createMember("포라", "fora@gmail.com", "1234");
    private final ReservationTime time = TestFixture.createTime(10, 0);
    private final Theme theme = TestFixture.createTheme("테마", "설명", "썸넬");

    @Test
    void 대기를_생성할_수_있다() {
        // given
        LocalDate date = LocalDate.of(2024, 3, 20);
        LocalDateTime createdAt = LocalDateTime.now();

        // when
        Waiting waiting = Waiting.createWithoutId(member, date, time, theme, createdAt);

        // then
        assertThat(waiting.getMember()).isEqualTo(member);
        assertThat(waiting.getDate()).isEqualTo(date);
        assertThat(waiting.getTime()).isEqualTo(time);
        assertThat(waiting.getTheme()).isEqualTo(theme);
        assertThat(waiting.getCreatedAt()).isEqualTo(createdAt);
    }
}
