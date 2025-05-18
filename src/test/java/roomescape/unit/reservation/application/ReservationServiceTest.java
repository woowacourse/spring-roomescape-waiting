package roomescape.unit.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.common.time.CurrentDateTime;
import roomescape.member.application.dto.MemberInfo;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationSearchCondition;
import roomescape.reservation.application.dto.ReservationTimeInfo;
import roomescape.reservation.application.dto.ThemeInfo;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.domain.reservation.Reservation;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.theme.ThemeRepository;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.time.ReservationTimeRepository;
import roomescape.support.fake.FakeMemberRepository;
import roomescape.support.fake.FakeReservationRepository;
import roomescape.support.fake.FakeReservationTimeRepository;
import roomescape.support.fake.FakeThemeRepository;
import roomescape.support.util.TestCurrentDateTime;


class ReservationServiceTest {

    private static final CurrentDateTime currentDateTime = new TestCurrentDateTime(LocalDateTime.of(2025, 4, 2, 11, 0));
    private static final LocalDate today = currentDateTime.getDate();
    private final LocalDate yesterday = today.minusDays(1);
    private final LocalDate tomorrow = today.plusDays(1);

    private final FakeReservationRepository reservationRepository = new FakeReservationRepository();
    private final ReservationTimeRepository reservationTimeRepository = new FakeReservationTimeRepository();
    private final ThemeRepository themeRepository = new FakeThemeRepository();
    private final MemberRepository memberRepository = new FakeMemberRepository();
    private final ReservationService reservationService = new ReservationService(reservationRepository,
            reservationTimeRepository, themeRepository, memberRepository, currentDateTime);

    private Member member1, member2;
    private ReservationTime time1, time2;
    private Theme theme1, theme2;

    @BeforeEach
    void setUp() {
        member1 = memberRepository.save(new Member(null, "리버1", "river1@gmail.com", "riverpw1", MemberRole.ADMIN));
        member2 = memberRepository.save(new Member(null, "리버2", "river2@gmail.com", "riverpw2", MemberRole.ADMIN));

        time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(11, 0)));
        time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));

        theme1 = themeRepository.save(new Theme(null, "우테코탈출1", "우테코탈출1 설명, ", "우테코탈출1 썸네일.jpg"));
        theme2 = themeRepository.save(new Theme(null, "우테코탈출2", "우테코탈출2 설명, ", "우테코탈출2 썸네일.jpg"));
    }

    @DisplayName("날짜와 시간과 테마가 중복되는 예약을 할 경우 예외가 발생한다")
    @Test
    void should_ThrowException_WhenDuplicateReservation() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        reservationService.createReservation(request);
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약이 존재합니다.");
    }

    @DisplayName("날짜와 시간이 같아도 테마가 다르면 예외가 발생하지 않는다")
    @Test
    void shouldNot_ThrowException_WhenThemeIsDifferent() {
        // given
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme2.id());
        reservationService.createReservation(request1);
        // when & then
        assertThatCode(() -> reservationService.createReservation(request2))
                .doesNotThrowAnyException();
    }

    @DisplayName("예약 시간이 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTime() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, member1.id(), 3L, theme1.id());
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("테마가 존재하지 않을 경우 예외가 발생한다")
    @Test
    void validateTheme() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(), 3L);
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마가 존재하지 않습니다.");
    }

    @DisplayName("과거 시간에 예약할 경우 예외가 발생한다")
    @Test
    void validatePastTime() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(yesterday, member1.id(), time1.id(),
                theme1.id());
        // when & then
        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간은 예약 불가합니다.");
    }

    @DisplayName("예약을 생성할 수 있다")
    @Test
    void create() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        // when
        final ReservationInfo response = reservationService.createReservation(request);
        // then
        final Reservation result = reservationRepository.findById(response.id());
        assertAll(
                () -> assertThat(result.date()).isEqualTo(response.date()),
                () -> assertThat(new MemberInfo(result.member())).isEqualTo(response.member()),
                () -> assertThat(new ReservationTimeInfo(result.time())).isEqualTo(response.time()),
                () -> assertThat(new ThemeInfo(result.theme())).isEqualTo(response.theme())
        );
    }

    @DisplayName("예약 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // given
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, member2.id(), time2.id(),
                theme2.id());
        reservationService.createReservation(request1);
        reservationService.createReservation(request2);
        // when
        final List<ReservationInfo> result = reservationService.getReservations();
        // then
        assertThat(result).hasSize(2);
    }

    @DisplayName("예약을 삭제할 수 있다")
    @Test
    void cancelById() {
        // given
        final ReservationCreateCommand request = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        final ReservationInfo response = reservationService.createReservation(request);
        // when
        reservationService.cancelReservationById(response.id());
        // then
        assertThat(reservationService.getReservations()).isEmpty();
    }

    @DisplayName("멤버 예약 목록을 조회할 수 있다")
    @Test
    void findReservationsByMember() {
        // given
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, member2.id(), time2.id(),
                theme2.id());
        final ReservationInfo response1 = reservationService.createReservation(request1);
        reservationService.createReservation(request2);
        // when
        final List<ReservationInfo> result = reservationService.findReservationsByMemberId(response1.id());
        // then
        assertThat(result).containsExactlyElementsOf(List.of(response1));
    }

    @DisplayName("기간을 지정하지 않은 예약 목록을 조회하다")
    @MethodSource(value = "searchConditions")
    @ParameterizedTest()
    void getReservationsOfNullDate(ReservationSearchCondition condition, int expectedSize) {
        // given
        final ReservationCreateCommand request1 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme1.id());
        final ReservationCreateCommand request2 = new ReservationCreateCommand(tomorrow, member1.id(), time1.id(),
                theme2.id());
        final ReservationCreateCommand request3 = new ReservationCreateCommand(tomorrow.plusDays(1), member2.id(),
                time1.id(), theme1.id());
        final ReservationCreateCommand request4 = new ReservationCreateCommand(tomorrow.plusDays(2), member1.id(),
                time1.id(), theme1.id());
        final ReservationCreateCommand request5 = new ReservationCreateCommand(tomorrow.plusDays(4), member1.id(),
                time1.id(), theme1.id());

        // when
        reservationService.createReservation(request1);
        reservationService.createReservation(request2);
        reservationService.createReservation(request3);
        reservationService.createReservation(request4);
        reservationService.createReservation(request5);

        // then
        assertThat(reservationService.getReservations(condition)).hasSize(expectedSize);
    }

    private static Stream<Arguments> searchConditions() {
        return Stream.of(
                Arguments.arguments(new ReservationSearchCondition(null, null, null, null), 5),
                Arguments.arguments(new ReservationSearchCondition(1L, null, null, null), 4),
                Arguments.arguments(new ReservationSearchCondition(null, 1L, null, null), 4),
                Arguments.arguments(new ReservationSearchCondition(1L, 1L, null, null), 3),
                Arguments.arguments(new ReservationSearchCondition(1L, 1L, today.plusDays(2), today.plusDays(4)), 1)
        );
    }
}

