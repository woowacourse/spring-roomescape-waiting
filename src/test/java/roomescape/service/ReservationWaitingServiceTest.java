package roomescape.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.exception.ExceptionType.DUPLICATE_WAITING;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
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
import roomescape.domain.Reservation;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
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
import roomescape.service.finder.ReservationFinder;

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
        ReservationFinder reservationFinder = new ReservationFinder(reservationRepository, reservationTimeRepository,
                memberRepository, themeRepository);
        waitingService = new ReservationWaitingService(new CollectionReservationWaitingRepository(), reservationFinder,
                memberRepository);
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
        ReservationFinder reservationFinder = new ReservationFinder(reservationRepository, reservationTimeRepository,
                memberRepository, themeRepository);
        waitingService = new ReservationWaitingService(new CollectionReservationWaitingRepository(), reservationFinder,
                memberRepository);
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

    @Test
    @DisplayName("자신의 예약 대기가 아닌 경우 지워지지 않는지 확인")
    void deleteFailWhenNotMine() {
        initServiceWithMember();
        themeRepository.save(DEFAULT_THEME);
        reservationTimeRepository.save(DEFAULT_TIME);
        reservationRepository.save(DEFAULT_RESERVATION);

        ReservationWaitingResponse response = waitingService.save(ReservationWaitingFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> waitingService.delete(DEFAULT_MEMBER.getId(), response.id()))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(PERMISSION_DENIED.getMessage());
    }

    @Test
    @DisplayName("관리자는 자신의 예약 대기가 아닌 경우에도 지울 수 있는지 확인")
    void deleteSuccessWhenAdmin() {
        initServiceWithMember();
        themeRepository.save(DEFAULT_THEME);
        reservationTimeRepository.save(DEFAULT_TIME);
        reservationRepository.save(
                new Reservation(1L, DEFAULT_ADMIN, DEFAULT_RESERVATION.getDate(), DEFAULT_TIME, DEFAULT_THEME));

        ReservationWaitingResponse response = waitingService.save(new ReservationRequest(DEFAULT_RESERVATION.getDate(),
                DEFAULT_MEMBER.getId(), DEFAULT_TIME.getId(), DEFAULT_THEME.getId()));

        assertDoesNotThrow(() -> waitingService.delete(DEFAULT_ADMIN.getId(), response.id()));
    }
}
