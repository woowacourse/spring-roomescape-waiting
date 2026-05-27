package roomescape.domain.time.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import roomescape.domain.reservation.entity.Reservation;
import roomescape.domain.reservation.repository.JdbcReservationRepository;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.entity.Theme;
import roomescape.domain.theme.repository.JdbcThemeRepository;
import roomescape.domain.theme.repository.ThemeRepository;
import roomescape.domain.time.dto.request.TimeCreateRequestDto;
import roomescape.domain.time.dto.response.TimeAvailabilityResponseDto;
import roomescape.domain.time.dto.response.TimeResponseDto;
import roomescape.domain.time.entity.Time;
import roomescape.domain.time.mapper.TimeMapper;
import roomescape.domain.time.repository.JdbcTimeRepository;
import roomescape.domain.time.repository.TimeRepository;
import roomescape.global.error.dto.ParameterErrorResponseDto;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.exception.GeneralNotFoundException;

class TimeServiceTest {

    private TimeService timeService;
    private TimeRepository timeRepository;
    private ThemeRepository themeRepository;
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:" + System.nanoTime() + ";MODE=MySQL;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);

        themeRepository = new JdbcThemeRepository(dataSource);
        timeRepository = new JdbcTimeRepository(dataSource);
        reservationRepository = new JdbcReservationRepository(dataSource);
        timeService = new TimeService(reservationRepository, timeRepository, themeRepository);
    }

    @Nested
    class GetTimesTest {

        @Test
        void 성공() {
            // given
            LocalTime startAt = LocalTime.of(10, 0);

            timeRepository.save(Time.create(startAt));
            timeRepository.save(Time.create(startAt.plusHours(1)));
            timeRepository.save(Time.create(startAt.plusHours(2)));

            // when
            List<TimeResponseDto> actual = timeService.getTimes();

            // then
            assertThat(actual).containsExactly(
                new TimeResponseDto(1L, startAt),
                new TimeResponseDto(2L, startAt.plusHours(1)),
                new TimeResponseDto(3L, startAt.plusHours(2))
            );
        }

        @Test
        void 예약_시간이_없으면_빈_목록을_반환한다() {
            // when
            List<TimeResponseDto> actual = timeService.getTimes();

            // then
            assertThat(actual).isEmpty();
        }
    }

    @Nested
    class SaveTimeTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                TimeCreateRequestDto request = new TimeCreateRequestDto(LocalTime.of(15, 30));

                // when
                TimeResponseDto actual = timeService.saveTime(request);

                // then
                assertThat(actual).isEqualTo(new TimeResponseDto(1L, LocalTime.of(15, 30)));
                assertThat(timeService.getTimes()).containsExactly(actual);
            }
        }

        @Nested
        class Failed {

            @Test
            void 이미_등록된_예약_시간이면_예외가_발생한다() {
                // given
                LocalTime startAt = LocalTime.of(15, 30);
                timeRepository.save(Time.create(startAt));
                TimeCreateRequestDto request = new TimeCreateRequestDto(startAt);

                // when & then
                assertThatThrownBy(() -> timeService.saveTime(request))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("이미 등록된 예약 시간입니다.");
            }
        }
    }

    @Nested
    class DeleteTimeByIdTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                Time savedTime = timeRepository.save(Time.create(LocalTime.of(12, 0)));
                timeRepository.save(Time.create(LocalTime.of(13, 0)));

                // when
                timeService.deleteTimeById(savedTime.getId());

                // then
                List<TimeResponseDto> actual = timeService.getTimes();
                assertThat(actual).containsExactly(new TimeResponseDto(2L, LocalTime.of(13, 0)));
            }
        }

        @Nested
        class Failed {

            @Test
            void 예약_시간_ID가_존재하지_않으면_예외가_발생한다() {
                // when & then
                assertThatThrownBy(() -> timeService.deleteTimeById(999L))
                    .isInstanceOf(GeneralException.class)
                    .hasMessage("예약 시간을 찾을 수 없습니다.");
            }
        }
    }

    @Nested
    class GetAvailableTimesTest {

        @Nested
        class Success {

            @Test
            void 성공() {
                // given
                Time time1 = timeRepository.save(Time.create(LocalTime.of(18, 0)));
                Time time2 = timeRepository.save(Time.create(LocalTime.of(19, 0)));
                Time time3 = timeRepository.save(Time.create(LocalTime.of(20, 0)));
                Time time4 = timeRepository.save(Time.create(LocalTime.of(21, 0)));

                Theme theme1 = themeRepository.save(Theme.create(
                    "셜록: 런던의 그림자",
                    "안개 낀 런던, 의문의 살인 사건 현장에 남겨진 마지막 단서를 찾아 범인을 검거하세요.",
                    "https://images.unsplash.com/photo-1585076641399-5c0f74268a2d?auto=format&fit=crop&q=80&w=800"
                ));
                Theme theme2 = themeRepository.save(Theme.create(
                    "폐쇄병동의 비밀",
                    "버려진 정신병원, 자정마다 들려오는 비명소리... 당신은 이곳에서 제정신으로 나갈 수 있을까요?",
                    "https://images.unsplash.com/photo-1509248961158-e54f6934749c?auto=format&fit=crop&q=80&w=800"
                ));
                Theme theme3 = themeRepository.save(Theme.create(
                    "파라오의 저주",
                    "모래 폭풍 속에 발견된 고대 피라미드. 함정을 피하고 숨겨진 황금 마스크를 찾아 탈출해야 합니다.",
                    "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&q=80&w=800"
                ));
                themeRepository.save(Theme.create(
                    "이상한 나라의 초대장",
                    "토끼굴로 떨어진 당신, 모든 것이 거꾸로 된 세상에서 원래 세계로 돌아가는 길을 찾으세요.",
                    "https://images.unsplash.com/photo-1582139329536-e7284fece509?auto=format&fit=crop&q=80&w=800"
                ));

                reservationRepository.save(Reservation.create("브라이언", LocalDate.of(2026, 5, 10), time1, theme1));
                reservationRepository.save(Reservation.create("제이슨", LocalDate.of(2026, 5, 10), time2, theme2));
                reservationRepository.save(Reservation.create("앨리스", LocalDate.of(2026, 5, 11), time3, theme3));
                reservationRepository.save(Reservation.create("데이브", LocalDate.of(2026, 5, 11), time4, theme1));

                LocalDate date = LocalDate.of(2026, 5, 10);
                Long themeId = 1L;
                List<TimeAvailabilityResponseDto> expected = List.of(
                    TimeMapper.toAvailabilityResponseDto(time1, false),
                    TimeMapper.toAvailabilityResponseDto(time2, true),
                    TimeMapper.toAvailabilityResponseDto(time3, true),
                    TimeMapper.toAvailabilityResponseDto(time4, true));

                // when
                List<TimeAvailabilityResponseDto> actual = timeService.getTimeAvailabilities(date, themeId);

                // then
                assertThat(actual).isEqualTo(expected);
            }
        }

        @Nested
        class Failed {

            @Test
            void themeId가_존재하지_않으면_예외가_발생한다() {
                // given
                LocalDate date = LocalDate.of(2026, 5, 10);
                Long themeId = 999L;

                // when & then
                assertThatThrownBy(() -> timeService.getTimeAvailabilities(date, themeId))
                    .isInstanceOfSatisfying(GeneralNotFoundException.class, exception -> {
                        assertThat(exception).hasMessage("조회할 자원이 존재하지 않습니다.");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::parameter)
                            .containsExactly("themeId");
                        assertThat(exception.getParameterErrors())
                            .extracting(ParameterErrorResponseDto::message)
                            .containsExactly("존재 하지 않는 테마입니다.");
                    });
            }
        }
    }
}
