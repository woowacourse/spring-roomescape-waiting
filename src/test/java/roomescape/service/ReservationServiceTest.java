package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.PastTimeReservationException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.input.WaitingInput;
import roomescape.util.DatabaseCleaner;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @Autowired
    ReservationService reservationService;

    @Autowired
    MemberService memberService;

    @Autowired
    WaitingService waitingService;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다")
    void create_reservation() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final ReservationInput input = new ReservationInput("2023-03-13", timeId, themeId, memberId);

        assertThatCode(() -> reservationService.createReservation(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중복 예약 이면 예외를 발생한다.")
    void throw_exception_when_duplicate_reservationTime() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        reservationService.createReservation(new ReservationInput("2011-11-24", timeId, themeId, memberId));
        final var input = new ReservationInput("2011-11-24", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> reservationService.createReservation(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("지나간 날짜와 시간으로 예약 생성 시 예외가 발생한다.")
    void throw_exception_when_create_past_time_reservation() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final var input = new ReservationInput("1300-03-10", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> reservationService.createReservation(input))
                .isInstanceOf(PastTimeReservationException.class);
    }

    @Test
    @DisplayName("테마,멤버,날짜 범위에 맞는 예약을 검색한다.")
    void search_reservation_with_theme_member_and_date() {
        final Long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final Long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberService.createMember(MemberFixture.getUserCreateInput())
                .id();
        final var input1 = new ReservationInput("2024-05-10", timeId, themeId, memberId);
        final var input2 = new ReservationInput("2024-05-30", timeId, themeId, memberId);
        final var input3 = new ReservationInput("2024-05-15", timeId, themeRepository.save(ThemeFixture.getDomain())
                .getId(), memberId);
        reservationService.createReservation(input1);
        reservationService.createReservation(input2);
        reservationService.createReservation(input3);

        assertThat(reservationService.searchReservation(new ReservationSearchInput(themeId, memberId,
                LocalDate.parse("2024-05-01"), LocalDate.parse("2024-05-20"))))
                .hasSize(1);
    }

    @Test
    @DisplayName("내 예약 및 예약 대기 목록을 반환한다.")
    void get_all_my_reservations() {
        final Long timeId = reservationTimeRepository.save(ReservationTime.from(null, "10:00"))
                .getId();
        final Long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final var member1 = memberRepository.save(MemberFixture.getDomain());
        final var member2 = memberRepository.save(MemberFixture.getDomain("new2@gmail.com"));
        final var member3 = memberRepository.save(MemberFixture.getDomain("new3@gmail.com"));
        reservationService.createReservation(new ReservationInput("2024-05-30", timeId, themeId, member1.getId()));
        reservationService.createReservation(new ReservationInput("2024-06-30", timeId, themeId, member3.getId()));
        waitingService.createWaiting(new WaitingInput("2024-05-30", timeId, themeId, member2.getId()));
        waitingService.createWaiting(new WaitingInput("2024-05-30", timeId, themeId, member3.getId()));

        assertThat(reservationService.getAllMyReservations(member3))
                .hasSize(2);
    }
}
