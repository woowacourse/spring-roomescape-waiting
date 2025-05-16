package roomescape.service;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.exception.global.ConflictException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;
import roomescape.test.fixture.ReservationFixture;
import roomescape.test.fixture.ReservationTimeFixture;
import roomescape.test.fixture.UserFixture;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = AFTER_CLASS)
class ReservationServiceTest {

    @Autowired
    private ReservationService service;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private UserRepository userRepository;

    private Theme savedTheme;
    private User savedUser;

    @BeforeEach
    void beforeEach() {
        savedTheme = themeRepository.save(new Theme("name1", "dd", "tt"));
        savedUser = userRepository.save(UserFixture.create(Role.ROLE_MEMBER, "n1", "e1", "p1"));
    }

    private ReservationTime createAndSaveReservationTime(LocalTime time) {
        ReservationTime reservationTime = ReservationTimeFixture.create(time);
        return reservationTimeRepository.save(reservationTime);
    }

    private Reservation createReservation(int plusDays, ReservationTime time) {
        LocalDate date = LocalDate.now().plusDays(plusDays);
        return ReservationFixture.createByBookedStatus(date, time, savedTheme, savedUser);
    }

    private ReservationRequestDto createRequestDto(int plusDays, Long timeId, Long themeId) {
        LocalDate date = LocalDate.now().plusDays(plusDays);

        return ReservationFixture.createRequestDto(date, timeId, themeId);
    }

    @Nested
    @DisplayName("예약 추가하기 기능")
    class add {

        @DisplayName("이미 같은 시간에 예약이 존재한다면 예외 처리한다.")
        @Test
        void add_failure_byDuplicateDateTime() {
            // given
            ReservationTime reservationTime1 = createAndSaveReservationTime(LocalTime.of(11, 33));
            Reservation reservation1 = createReservation(1, reservationTime1);

            ReservationTime reservationTime2 = createAndSaveReservationTime(LocalTime.of(22, 44));
            Reservation reservation2 = createReservation(2, reservationTime2);

            reservationRepository.save(reservation1);
            reservationRepository.save(reservation2);

            // when & then
            LocalDate duplicateDate = reservation1.getDate();
            Long duplicateReservationTimeId = reservationTime1.getId();
            ReservationRequestDto requestDto = ReservationFixture.createRequestDto(duplicateDate,
                    duplicateReservationTimeId, savedTheme.getId());

            Assertions.assertThatThrownBy(
                    () -> service.add(requestDto, savedUser)
            ).isInstanceOf(ConflictException.class);
        }

        @DisplayName("시간이 같아도 날짜가 다르다면 예약이 가능하다.")
        @Test
        void add_success_withDifferenceDateAndSameTime() {
            // given
            ReservationTime reservationTime1 = createAndSaveReservationTime(LocalTime.of(11, 33));
            Reservation reservation1 = createReservation(1, reservationTime1);

            ReservationTime reservationTime2 = createAndSaveReservationTime(LocalTime.of(22, 44));
            Reservation reservation2 = createReservation(2, reservationTime2);

            reservationRepository.save(reservation1);
            reservationRepository.save(reservation2);

            // when & then
            Long duplicateReservationTimeId = reservationTime1.getId();
            ReservationRequestDto requestDto = createRequestDto(3, duplicateReservationTimeId,
                    savedTheme.getId());

            Assertions.assertThatCode(
                    () -> service.add(requestDto, savedUser)
            ).doesNotThrowAnyException();
        }
    }
}
