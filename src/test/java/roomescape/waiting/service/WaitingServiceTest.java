package roomescape.waiting.service;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;
import roomescape.waiting.domain.dto.WaitingRequestDto;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.exception.NotFoundWaitingException;
import roomescape.waiting.fixture.WaitingFixture;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = {
        MemberTestDataConfig.class,
        ReservationTimeTestDataConfig.class,
        ThemeTestDataConfig.class
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;
    @Autowired
    private ReservationRepository reservationRepository;

    private static User savedMember;
    private static ReservationTime savedTime;
    private static Theme savedTheme;

    @BeforeAll
    public static void setUp(
                             @Autowired MemberTestDataConfig memberTestDataConfig,
                             @Autowired ReservationTimeTestDataConfig reservationTimeTestDataConfig,
                             @Autowired ThemeTestDataConfig themeTestDataConfig
    ) {
        savedMember = memberTestDataConfig.getSavedUser();
        savedTime = reservationTimeTestDataConfig.getSavedReservationTime();
        savedTheme = themeTestDataConfig.getSavedTheme();
    }

    @Nested
    @DisplayName("예약 대기 추가 기능")
    class create {

        @DisplayName("예약 대기 추가 요청이 들어왔을 때 예약 대기 객체가 잘 생성되는 지")
        @Test
        void create_success() {
            // given
            Reservation reservation = ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2),
                    savedTime,
                    savedTheme, savedMember);
            Reservation savedReservation = reservationRepository.save(reservation);
            WaitingRequestDto requestDto = WaitingFixture.createReqDto(savedReservation.getDate(),
                    savedReservation.getReservationTime().getId(),
                    savedReservation.getTheme().getId());

            // when
            WaitingResponseDto responseDto = waitingService.create(requestDto, savedMember);

            // then
            SoftAssertions.assertSoftly(softly -> {
                softly.assertThat(responseDto.date()).isEqualTo(requestDto.date());
                softly.assertThat(responseDto.time().id()).isEqualTo(requestDto.timeId());
                softly.assertThat(responseDto.theme().id()).isEqualTo(requestDto.themeId());
            });
        }
    }

    @Nested
    @DisplayName("예약 대기 삭제 기능")
    class deleteById {

        @DisplayName("존재하는 예약 대기 id 요청했을 때 삭제 가능하다")
        @Test
        void deleteById_success_byExistingWaitingId() {
            // given
            Reservation reservation = ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2),
                    savedTime,
                    savedTheme, savedMember);
            Reservation savedReservation = reservationRepository.save(reservation);
            WaitingRequestDto requestDto = WaitingFixture.createReqDto(savedReservation.getDate(),
                    savedReservation.getReservationTime().getId(),
                    savedReservation.getTheme().getId());

            WaitingResponseDto waitingResponseDto = waitingService.create(requestDto, savedMember);

            // before then
            Assertions.assertThat(waitingService.findAll()).hasSize(1);

            // when
            waitingService.delete(waitingResponseDto.id());

            // then
            Assertions.assertThat(waitingService.findAll()).hasSize(0);
        }

        @DisplayName("존재하지 않는 예약 대기 id로 삭제 요청했을 때 예외가 발생한다 : NotFoundWaitingException")
        @Test
        void deleteById_throwException_byNonExistingWaitingId() {
            // given
            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> waitingService.delete(Long.MAX_VALUE)
            ).isInstanceOf(NotFoundWaitingException.class);
        }
    }
}
