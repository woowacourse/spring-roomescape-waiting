package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.PastTimeReservationException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.*;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;
import roomescape.util.DatabaseCleaner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ReservationService sut;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    DatabaseCleaner databaseCleaner;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationInfoRepository reservationInfoRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다")
    @Transactional
    void create_reservation() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from("10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberRepository.save(MemberFixture.getDomain())
                .getId();
        final ReservationInput input = new ReservationInput("2023-03-13", timeId, themeId, memberId);

        assertThatCode(() -> sut.createReservation(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("중복 예약 이면 예외를 발생한다.")
    @Transactional
    void throw_exception_when_duplicate_reservationTime() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from("10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final long memberId = memberRepository.save(MemberFixture.getDomain())
                .getId();
        sut.createReservation(new ReservationInput("2011-11-24", timeId, themeId, memberId));
        final var input = new ReservationInput("2011-11-24", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> sut.createReservation(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("지나간 날짜와 시간으로 예약 생성 시 예외가 발생한다.")
    void throw_exception_when_create_past_time_reservation() {
        final long timeId = reservationTimeRepository.save(ReservationTime.from("10:00"))
                .getId();
        final long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberRepository.save(MemberFixture.getDomain())
                .getId();
        final var input = new ReservationInput("1300-03-10", timeId, themeId, memberId);

        assertThatThrownBy(
                () -> sut.createReservation(input))
                .isInstanceOf(PastTimeReservationException.class);
    }

    @Test
    @DisplayName("테마,멤버,날짜 범위에 맞는 예약을 검색한다.")
    @Transactional
    void search_reservation_with_theme_member_and_date() {
        final Long timeId = reservationTimeRepository.save(ReservationTime.from("10:00"))
                .getId();
        final Long themeId = themeRepository.save(ThemeFixture.getDomain())
                .getId();
        final var memberId = memberRepository.save(MemberFixture.getDomain())
                .getId();
        final var input1 = new ReservationInput("2024-05-10", timeId, themeId, memberId);
        final var input2 = new ReservationInput("2024-05-30", timeId, themeId, memberId);
        final var input3 = new ReservationInput("2024-05-15", timeId, themeRepository.save(ThemeFixture.getDomain())
                .getId(), memberId);
        sut.createReservation(input1);
        sut.createReservation(input2);
        sut.createReservation(input3);

        assertThat(sut.searchReservation(new ReservationSearchInput(themeId, memberId,
                LocalDate.parse("2024-05-01"), LocalDate.parse("2024-05-20"))))
                .hasSize(1);
    }

    @Test
    @DisplayName("예약자가 아니면 취소할 수 없다.")
    void reservation_cancel_make_next_waiting_reservations() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain("joyson5582@email.com"));
        final Member member2 = memberRepository.save(MemberFixture.getDomain("alphaka@email.com"));

        final ReservationOutput reservationOutputs =
                sut.createReservation(new ReservationInput("2025-01-01", time.getId(), theme.getId(), member.getId()));

        assertThatThrownBy(() -> sut.deleteReservation(reservationOutputs.id(), member2.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }


    @Test
    @DisplayName("예약자가 취소하면 다음 대기자가 예약자가 된다.")
    void reservation_cancel_make_next_waiting_reservation() {
        final ReservationTime time = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain("joyson5582@email.com"));
        final Member member2 = memberRepository.save(MemberFixture.getDomain("alphaka@email.com"));

        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from("2025-01-01", time, theme));

        final ReservationOutput reservationOutputs =
                sut.createReservation(new ReservationInput("2025-01-01", time.getId(), theme.getId(), member.getId()));

        waitingRepository.save(new Waiting(member2, reservationInfo));
        sut.deleteReservation(reservationOutputs.id(), member.getId());

        final var result = reservationRepository.findAllByMemberId(member2.getId())
                .stream()
                .anyMatch(reservation ->
                        reservation.getMember()
                                .equals(member2) && reservation.getReservationInfo()
                                .equals(reservationInfo)
                );

        assertThat(result).isTrue();
    }
}
