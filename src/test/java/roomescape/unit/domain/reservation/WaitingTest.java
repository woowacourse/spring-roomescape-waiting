package roomescape.unit.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberEncodedPassword;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberRole;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationSchedule;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.domain.time.ReservationTime;

class WaitingTest {

    private final ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
    private final Theme theme = new Theme(
            1L,
            new ThemeName("공포"),
            new ThemeDescription("무섭다"),
            new ThemeThumbnail("thumb.jpg")
    );
    private final Member member = new Member(
            1L,
            new MemberName("홍길동"),
            new MemberEmail("test@email.com"),
            new MemberEncodedPassword("encoded"),
            MemberRole.MEMBER
    );
    private final ReservationSchedule schedule = new ReservationSchedule(
            new ReservationDate(LocalDate.now()),
            time,
            theme
    );
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    void member는_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new Waiting(1L, null, schedule, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void schedule은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new Waiting(1L, member, null, now))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void waitStartAt은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new Waiting(1L, member, schedule, null))
                .isInstanceOf(NullPointerException.class);
    }
} 
