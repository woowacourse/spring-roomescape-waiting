package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_WAITING;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_RESERVATION;
import static roomescape.fixture.MemberFixture.DEFAULT_ADMIN;
import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ReservationWaitingFixture;
import roomescape.repository.CollectionMemberRepository;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;
import roomescape.repository.CollectionReservationWaitingRepository;
import roomescape.repository.CollectionThemeRepository;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

class ReservationWaitingServiceTest {
    private ReservationRepository reservationRepository;
    private ReservationWaitingService waitingService;
    private ReservationTimeRepository reservationTimeRepository;
    private MemberRepository memberRepository;
    private ThemeRepository themeRepository = new CollectionThemeRepository();

    @BeforeEach
    void initService() {
        reservationRepository = new CollectionReservationRepository();
        reservationTimeRepository = new CollectionReservationTimeRepository();
        themeRepository = new CollectionThemeRepository();
        memberRepository = new CollectionMemberRepository();
        waitingService = new ReservationWaitingService(
                reservationRepository,
                reservationTimeRepository,
                memberRepository,
                themeRepository,
                new CollectionReservationWaitingRepository()
        );
    }

    @Test
    @DisplayName("없는 시간에 예약 대기 시도시 실패하는지 확인")
    void saveFailWhenTimeNotFound() {
        Assertions.assertThatThrownBy(() -> waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @Test
    @DisplayName("없는 테마에 예약 대기 시도시 실패하는지 확인")
    void saveFailWhenThemeNotFound() {
        reservationTimeRepository.save(DEFAULT_TIME);

        Assertions.assertThatThrownBy(() -> waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_THEME.getMessage());
    }

    @Test
    @DisplayName("없는 회원 예약 대기 시도시 실패하는지 확인")
    void saveFailWhenMemberNotFound() {
        reservationTimeRepository.save(DEFAULT_TIME);
        themeRepository.save(DEFAULT_THEME);

        Assertions.assertThatThrownBy(() -> waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    @DisplayName("예약이 없는데 예약 대기 시도시 실패하는지 확인")
    void saveFailWhenNullReservation() {
        initServiceWithMember();
        themeRepository.save(DEFAULT_THEME);
        reservationTimeRepository.save(DEFAULT_TIME);

        Assertions.assertThatThrownBy(() -> waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(WAITING_WITHOUT_RESERVATION.getMessage());
    }

    void initServiceWithMember() {
        reservationRepository = new CollectionReservationRepository();
        reservationTimeRepository = new CollectionReservationTimeRepository();
        themeRepository = new CollectionThemeRepository();
        memberRepository = new CollectionMemberRepository(List.of(DEFAULT_ADMIN, DEFAULT_MEMBER));
        waitingService = new ReservationWaitingService(
                reservationRepository,
                reservationTimeRepository,
                memberRepository,
                themeRepository,
                new CollectionReservationWaitingRepository()
        );
    }

    @Test
    @DisplayName("중복된 예약 대기 시도시 실패하는지 확인")
    void saveFailWhenDuplicateWaiting() {
        initServiceWithMember();
        themeRepository.save(DEFAULT_THEME);
        reservationTimeRepository.save(DEFAULT_TIME);
        reservationRepository.save(DEFAULT_RESERVATION);

        waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(DUPLICATE_WAITING.getMessage());
    }
}
