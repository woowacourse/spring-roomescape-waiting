package roomescape.service;

import static roomescape.exception.ExceptionType.DELETE_USED_THEME;
import static roomescape.exception.ExceptionType.DUPLICATE_THEME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
@Transactional
class ThemeServiceTest {
    @Autowired
    private ThemeService themeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("중복된 테마를 생성할 수 없는지 확인")
    void saveFailWhenDuplicate() {
        themeService.save(ThemeFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> themeService.save(ThemeFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_THEME.getMessage());
    }

    @Test
    @DisplayName("이미 예약이 있는 테마를 지울 수 없는지 확인")
    void deleteFailWhenUsed() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);

        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), time, theme));

        Assertions.assertThatThrownBy(() -> themeService.delete(theme.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_THEME.getMessage());
    }
}
