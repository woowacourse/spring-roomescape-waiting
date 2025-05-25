package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.exception.BadRequestException;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.WaitingRequest;
import roomescape.presentation.dto.WaitingResponse;
import roomescape.util.CurrentUtil;

@SpringBootTest
@Transactional
@Sql("classpath:data-waitingService.sql")
class WaitingServiceTest {

    @Autowired
    private WaitingService waitingService;

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private QueryService queryService;

    @Autowired
    private LoginMember loginMember;

    @Autowired
    private CurrentUtil currentUtil;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public LoginMember loginMember() {
            return new LoginMember(100L, "미미", "test1@example.com", "USER");
        }
        @Bean
        public CurrentUtil currentUtil() {
            return new CurrentUtil() {
                @Override
                public LocalDate getCurrentDate() {
                    return LocalDate.of(2025, 5, 10);
                }

                @Override
                public LocalDateTime getCurrentDateTime() {
                    return LocalDateTime.of(2025, 5, 10, 12, 0);
                }
            };
        }
    }

    @Test
    @DisplayName("예약 대기를 등록한다")
    void insert() {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 5, 15), 100L, 100L);

        // when
        WaitingResponse response = waitingService.insert(loginMember, request);

        // then
        assertAll(
                () -> assertThat(response.theme().getId()).isEqualTo(100L),
                () -> assertThat(response.time().getId()).isEqualTo(100L),
                () -> assertThat(response.date()).isEqualTo(LocalDate.of(2025, 5, 15)),
                () -> assertThat(response.member().getId()).isEqualTo(loginMember.id())
        );
    }

    @Test
    @DisplayName("이미 해당 사용자의 예약이 있다면 예약 대기를 등록할 수 없다")
    void insertWhenReservationExists() {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 5, 13), 100L, 100L);

        // when & then
        assertThatThrownBy(() -> waitingService.insert(loginMember, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("사용자가 예약한 항목입니다");
    }

    @Test
    @DisplayName("이미 동일 조건의 예약 대기가 존재하면 예외가 발생한다")
    void insertWhenWaitingAlreadyExists() {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 5, 11), 100L, 100L);

        // when & then
        assertThatThrownBy(() -> waitingService.insert(loginMember, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미 예약 대기를 하였습니다");
    }

    @Test
    @DisplayName("예약 대기를 삭제한다")
    void deleteById() {
        // given
        WaitingRequest request = new WaitingRequest(LocalDate.of(2025, 5, 15), 100L, 100L);
        WaitingResponse response = waitingService.insert(loginMember, request);
        Long id = response.id();

        // when
        waitingService.deleteById(id);

        // then
        assertThat(waitingRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("과거 예약 대기는 삭제할 수 없다")
    void deleteWhenPast() {
        // given
        Long pastWaitingId = 110L;

        // when & then
        assertThatThrownBy(() -> waitingService.deleteById(pastWaitingId))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이전 예약 대기는 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("전체 예약 대기를 조회한다")
    void findAll() {
        // given

        // when
        List<WaitingResponse> waitingResponses = waitingService.findAll();

        // then
        assertThat(waitingResponses).hasSize(5);
    }
}
