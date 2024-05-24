package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.exception.ExceptionType.DELETE_USED_TIME;
import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION_TIME;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_REQUEST;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
class ReservationTimeServiceTest {
    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @AfterEach
    void cleanUp() {
        reservationRepository.deleteAll();
        reservationTimeRepository.deleteAll();
        themeRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("중복된 예약 시간을 생성할 수 없는지 확인")
    void saveFailWhenDuplicate() {
        reservationTimeService.save(DEFAULT_REQUEST);

        assertThatThrownBy(() -> reservationTimeService.save(DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_RESERVATION_TIME.getMessage());
    }

    @Test
    @DisplayName("예약 시간을 사용하는 예약이 있으면 예약을 삭제할 수 없다.")
    void deleteFailWhenUsed() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);

        reservationRepository.save(new Reservation(member, LocalDate.now().plusDays(1), time, theme));

        assertThatCode(() -> reservationTimeService.delete(time.getId()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DELETE_USED_TIME.getMessage());
    }
}
