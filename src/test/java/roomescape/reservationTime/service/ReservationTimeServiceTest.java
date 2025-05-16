package roomescape.reservationTime.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.request.ReservationRequestDto;
import roomescape.dto.response.ReservationTimeResponseDto;
import roomescape.exception.global.NotFoundException;
import roomescape.exception.local.AlreadyReservedTimeException;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;
import roomescape.reservationTime.fixture.ReservationTimeFixture;
import roomescape.service.ReservationService;
import roomescape.service.ReservationTimeService;
import roomescape.user.fixture.UserFixture;

@DataJpaTest
@Import({
        ReservationTimeService.class,
        ReservationService.class
})
class ReservationTimeServiceTest {

    private static final LocalTime TIME_FIELD = LocalTime.of(2, 40);

    @Autowired
    private ReservationTimeService service;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ReservationTime savedReservationTime;
    private Long savedReservationTimeId;

    @BeforeEach
    void setUp() {
        ReservationTime reservationTime = ReservationTimeFixture.create(TIME_FIELD);
        savedReservationTime = entityManager.persist(reservationTime);
        savedReservationTimeId = savedReservationTime.getId();
        entityManager.flush();
    }

    @DisplayName("ReservationTime 객체를 ReservationTimeResponseDto로 변환할 수 있다")
    @Test
    void convertToReservationTimeResponseDto() {
        // given
        ReservationTime reservationTime = ReservationTimeFixture.create(TIME_FIELD);

        // when
        ReservationTimeResponseDto resDto = ReservationTimeFixture.createResponseDto(reservationTime);

        // then
        Assertions.assertThat(resDto.startAt()).isEqualTo(TIME_FIELD);
    }

    private void deleteByIdAll() {
        jdbcTemplate.update("delete from reservation_time");
    }

    @Nested
    @DisplayName("저장된 모든 예약 시간 불러오는 기능")
    class findAll {

        @DisplayName("데이터가 있을 때 모든 예약 시간을 불러온다")
        @Test
        void findAll_success_whenDataExists() {
            // when
            List<ReservationTimeResponseDto> resDtos = service.findAll();

            // then
            assertSoftly(s -> {
                        s.assertThat(resDtos).hasSize(1);
                        s.assertThat(resDtos)
                                .extracting(ReservationTimeResponseDto::startAt)
                                .contains(TIME_FIELD);
                        resDtos.forEach(resDto ->
                                s.assertThat(resDto.id()).isNotNull());
                    }
            );
        }

        @DisplayName("데이터가 없더라도 예외 없이 빈 리스트를 반환한다")
        @Test
        void findAll_success_whenNoData() {
            // given
            deleteByIdAll();

            // when
            List<ReservationTimeResponseDto> resDtos = service.findAll();

            // then
            Assertions.assertThat(resDtos).hasSize(0);
        }
    }

    @Nested
    @DisplayName("예약 시간 추가 기능")
    class add {

        @DisplayName("유효한 입력일 시 예약 시간이 추가된다")
        @Test
        void add_success_whenValidInput() {
            // given
            LocalTime dummyTime1 = LocalTime.of(12, 33);

            // when
            service.add(ReservationTimeFixture.createRequestDto(dummyTime1));

            // then
            List<ReservationTimeResponseDto> resDtos = service.findAll();
            Assertions.assertThat(resDtos)
                    .extracting(ReservationTimeResponseDto::startAt)
                    .contains(dummyTime1);
        }

        @DisplayName("이미 등록되어 있는 예약 시간으로 추가 요청 시 예외 발생한다")
        @Test
        void add_throwException_byDuplicationReservationTime() {
            // given
            LocalTime dummyTime1 = LocalTime.of(12, 33);
            service.add(ReservationTimeFixture.createRequestDto(dummyTime1));

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> service.add(ReservationTimeFixture.createRequestDto(dummyTime1))
            ).isInstanceOf(DuplicateReservationException.class);
        }
    }

    @Nested
    @DisplayName("예약 시간 삭제 기능")
    class deleteById {

        @DisplayName("존재하는 id로 요청 시 예약 시간이 삭제된다.")
        @Test
        void deleteById_success_withValidId() {
            // given
            service.deleteById(savedReservationTimeId);

            // when
            List<ReservationTimeResponseDto> resDtos = service.findAll();

            // then
            Assertions.assertThat(resDtos).hasSize(0);
        }

        @DisplayName("존재하지 않는 id로 요청 시 예외가 발생한다")
        @Test
        void deleteById_throwException_whenIdNotFound() {
            // given
            // when
            // then
            Assertions.assertThatCode(
                    () -> service.deleteById(Long.MAX_VALUE)
            ).isInstanceOf(NotFoundException.class);
        }

        @DisplayName("예약에서 사용 중인 시간 삭제 시 예외가 발생한다")
        @Test
        void deleteById_throwException_whenUsingInReservation() {
            // given
            Theme theme = themeRepository.save(new Theme("name1", "dd", "tt"));
            User savedUser = userRepository.save(UserFixture.create(Role.ROLE_MEMBER, "n1", "e1", "p1"));

            reservationService.add(
                    new ReservationRequestDto(
                            LocalDate.now().plusMonths(3),
                            savedReservationTimeId,
                            theme.getId()
                    ), savedUser);

            // when, then
            Assertions.assertThatCode(
                    () -> service.deleteById(savedReservationTimeId)
            ).isInstanceOf(AlreadyReservedTimeException.class);
        }
    }
}
