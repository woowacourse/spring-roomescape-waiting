package roomescape.service;

import static roomescape.fixture.MemberFixture.DEFAULT_MEMBER;
import static roomescape.fixture.ReservationTimeFixture.DEFAULT_TIME;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.ReservationRequest;
import roomescape.exception.ExceptionType;
import roomescape.exception.RoomescapeException;
import roomescape.fixture.ReservationFixture;
import roomescape.repository.CollectionMemberRepository;
import roomescape.repository.CollectionReservationRepository;
import roomescape.repository.CollectionReservationTimeRepository;
import roomescape.repository.CollectionThemeRepository;

class ReservationServiceTest {
    private ReservationService reservationService;
    private CollectionReservationTimeRepository reservationTimeRepository;
    private CollectionThemeRepository themeRepository;
    private CollectionMemberRepository memberRepository;

    @BeforeEach
    void initService() {
        reservationTimeRepository = new CollectionReservationTimeRepository();
        themeRepository = new CollectionThemeRepository();
        memberRepository = new CollectionMemberRepository();
        reservationService = new ReservationService(
                new CollectionReservationRepository(),
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
    }

    @Test
    @DisplayName("없는 시간에 예약 시도시 실패하는지 확인")
    void saveFailWhenTimeNotFound() {
        Assertions.assertThatThrownBy(() -> reservationService.save(ReservationFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_RESERVATION_TIME.getMessage());
    }

    @Test
    @DisplayName("없는 테마에 예약 시도시 실패하는지 확인")
    void saveFailWhenThemeNotFound() {
        reservationTimeRepository.save(DEFAULT_TIME);

        Assertions.assertThatThrownBy(() -> reservationService.save(ReservationFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_THEME.getMessage());
    }

    @Test
    @DisplayName("없는 회원 예약 시도시 실패하는지 확인")
    void saveFailWhenMemberNotFound() {
        reservationTimeRepository.save(DEFAULT_TIME);
        themeRepository.save(DEFAULT_THEME);

        Assertions.assertThatThrownBy(() -> reservationService.save(ReservationFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.NOT_FOUND_MEMBER.getMessage());
    }

    @Test
    @DisplayName("중복된 예약 시도시 실패하는지 확인")
    void saveFailWhenDuplicateReservation() {
        reservationService = initServiceWithMember();
        reservationTimeRepository.save(DEFAULT_TIME);
        themeRepository.save(DEFAULT_THEME);

        reservationService.save(ReservationFixture.DEFAULT_REQUEST);

        Assertions.assertThatThrownBy(() -> reservationService.save(ReservationFixture.DEFAULT_REQUEST))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.DUPLICATE_RESERVATION.getMessage());
    }

    private ReservationService initServiceWithMember() {
        memberRepository = new CollectionMemberRepository(List.of(DEFAULT_MEMBER));
        reservationService = new ReservationService(
                new CollectionReservationRepository(),
                reservationTimeRepository,
                themeRepository,
                memberRepository
        );
        return reservationService;
    }

    @Test
    @DisplayName("이미 지나간 시간에 예약 시도시 실패하는지 확인")
    void saveFailWhenPastTime() {
        reservationService = initServiceWithMember();
        reservationTimeRepository.save(DEFAULT_TIME);
        themeRepository.save(DEFAULT_THEME);
        ReservationRequest reservationRequestWithPastDate = new ReservationRequest(LocalDate.now().minusDays(1),
                DEFAULT_MEMBER.getId(), DEFAULT_TIME.getId(), DEFAULT_THEME.getId());
        
        Assertions.assertThatThrownBy(() -> reservationService.save(reservationRequestWithPastDate))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(ExceptionType.PAST_TIME_RESERVATION.getMessage());
    }
}
