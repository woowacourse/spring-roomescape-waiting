package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Member;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationRequest;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest
class ReservationServiceTest {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @AfterEach
    void cleanUp() {
        reservationRepository.deleteAll();
        reservationTimeRepository.deleteAll();
        themeRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("없는 시간에 예약 시도시 실패하는지 확인")
    void saveFailWhenTimeNotFound() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        Theme theme = themeRepository.save(DEFAULT_THEME);

        ReservationTime notSavedTime = DEFAULT_TIME;
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), member.getId(), notSavedTime.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.save(reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @Test
    @DisplayName("없는 테마에 예약 시도시 실패하는지 확인")
    void saveFailWhenThemeNotFound() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);

        Theme notSavedTheme = DEFAULT_THEME;
        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), member.getId(), time.getId(), notSavedTheme.getId());

        assertThatThrownBy(() -> reservationService.save(reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_THEME.getMessage());
    }

    @Test
    @DisplayName("없는 회원 예약 시도시 실패하는지 확인")
    void saveFailWhenMemberNotFound() {
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);

        Member notSavedMember = DEFAULT_MEMBER;

        ReservationRequest reservationRequest = new ReservationRequest(
                LocalDate.now().plusDays(1), notSavedMember.getId(), time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.save(reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    @DisplayName("중복된 예약 시도시 실패하는지 확인")
    void saveFailWhenDuplicateReservation() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);
        LocalDate date = LocalDate.now().plusDays(1);

        ReservationRequest reservationRequest = new ReservationRequest(date, member.getId(), time.getId(), theme.getId());

        reservationService.save(reservationRequest);

        assertThatThrownBy(() -> reservationService.save(reservationRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.DUPLICATE_RESERVATION.getMessage());
    }

    @Test
    @DisplayName("이미 지나간 시간에 예약 시도시 실패하는지 확인")
    void saveFailWhenPastTime() {
        Member member = memberRepository.save(DEFAULT_MEMBER);
        ReservationTime time = reservationTimeRepository.save(DEFAULT_TIME);
        Theme theme = themeRepository.save(DEFAULT_THEME);
        LocalDate pastDate = LocalDate.now().minusDays(1);

        ReservationRequest reservationRequestWithPastDate = new ReservationRequest(
                pastDate, member.getId(), time.getId(), theme.getId());

        assertThatThrownBy(() -> reservationService.save(reservationRequestWithPastDate))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.PAST_TIME_RESERVATION.getMessage());
    }
}
