package roomescape.date.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.common.auth.jwt.JwtExtractor;
import roomescape.common.auth.jwt.JwtProvider;
import roomescape.common.auth.jwt.JwtValidator;
import roomescape.date.domain.ReservationDate;
import roomescape.date.exception.ReservationDateException;
import roomescape.date.service.ReservationDateService;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberRepository;

@WebMvcTest(ReservationDateAdminController.class)
class ReservationDateAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationDateService reservationDateService;

    @MockitoBean
    private JwtValidator jwtValidator;

    @MockitoBean
    private JwtExtractor jwtExtractor;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private MemberRepository memberRepository;

    private String managerToken;

    @BeforeEach
    void setUp() {
        managerToken = "Bearer mock-manager-token";
        when(jwtExtractor.extractJwtToken(any())).thenReturn(Optional.of("mock-manager-token"));
        when(jwtExtractor.getRole("mock-manager-token")).thenReturn(Role.MANAGER.name());
        when(jwtValidator.validateJwtToken("mock-manager-token")).thenReturn(true);
    }


    @Nested
    @DisplayName("create 메서드는")
    class CreateTest {


        @Test
        @DisplayName("예약을 생성한다")
        void 성공() throws Exception {
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationDate reservationDate = ReservationDate.load(1L, date, true);

            when(reservationDateService.register(date))
                .thenReturn(reservationDate);

            String request = """
                {
                  "date": "%s"
                }
                """.formatted(date);

            mockMvc.perform(post("/admin/dates")
                    .header(HttpHeaders.AUTHORIZATION, managerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationDate.getId()))
                .andExpect(jsonPath("$.date").value(date.toString()))
                .andExpect(jsonPath("$.isActive").value(reservationDate.isActive()));

            verify(reservationDateService).register(date);
        }


        @Test
        @DisplayName("요청 본문에 필수 필드가 누락되면 400을 반환한다")
        void 실패1() throws Exception {
            String request = """
                {
                  "date": null
                }
                """;

            mockMvc.perform(post("/admin/dates")
                    .header(HttpHeaders.AUTHORIZATION, managerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams[0].name").value("date"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("date는 필수 입력입니다."));

            verifyNoInteractions(reservationDateService);
        }


        @Test
        @DisplayName("이미 예약된 슬롯인 경우 409를 반환한다")
        void 실패2() throws Exception {
            LocalDate date = LocalDate.of(2026, 5, 20);

            when(reservationDateService.register(date))
                .thenThrow(new ReservationDateException(DATE_ALREADY_EXISTS));

            String request = """
                {
                  "date": "%s"
                }
                """.formatted(date);

            mockMvc.perform(post("/admin/dates")
                    .header(HttpHeaders.AUTHORIZATION, managerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(DATE_ALREADY_EXISTS.getHttpStatus().value()))
                .andExpect(jsonPath("$.error").value(DATE_ALREADY_EXISTS.getHttpStatus().name()))
                .andExpect(jsonPath("$.errorCode").value(DATE_ALREADY_EXISTS.getErrorCode()))
                .andExpect(jsonPath("$.message").value(DATE_ALREADY_EXISTS.getMessage()));

            verify(reservationDateService).register(date);
        }
    }

    @Nested
    @DisplayName("updateStatus 메서드는")
    class UpdateStatusTest {


        @Test
        @DisplayName("예약 정보를 변경한다")
        void 성공() throws Exception {
            LocalDate date = LocalDate.of(2026, 5, 20);
            boolean changeStatus = false;
            ReservationDate reservationDate = ReservationDate.load(1L, date, changeStatus);

            when(reservationDateService.updateStatus(reservationDate.getId(), changeStatus))
                .thenReturn(reservationDate);

            String request = """
                {
                  "isActive": %s
                }
                """.formatted(changeStatus);

            mockMvc.perform(patch("/admin/dates/{id}/status", reservationDate.getId())
                    .header(HttpHeaders.AUTHORIZATION, managerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationDate.getId()))
                .andExpect(jsonPath("$.date").value(date.toString()))
                .andExpect(jsonPath("$.isActive").value(changeStatus));

            verify(reservationDateService).updateStatus(reservationDate.getId(), changeStatus);
        }


        @Test
        @DisplayName("요청 본문에 필수 필드가 없는 경우 400을 반환한다")
        void 실패() throws Exception {
            String request = """
                {
                  "isActive": null
                }
                """;

            mockMvc.perform(patch("/admin/dates/1/status")
                    .header(HttpHeaders.AUTHORIZATION, managerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams[0].name").value("isActive"))
                .andExpect(jsonPath("$.invalidParams[0].reason").value("isActive는 필수 입력입니다."));

            verifyNoInteractions(reservationDateService);
        }
    }

    @Nested
    @DisplayName("getReservationDates 메서드는")
    class GetReservationDatesTest {


        @Test
        @DisplayName("예약 날짜를 조회한다")
        void 성공() throws Exception {
            LocalDate date = LocalDate.of(2026, 5, 20);
            ReservationDate reservationDate = ReservationDate.load(1L, date, true);

            when(reservationDateService.readDates())
                .thenReturn(List.of(reservationDate));

            mockMvc.perform(get("/admin/dates").header(HttpHeaders.AUTHORIZATION, managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(reservationDate.getId()))
                .andExpect(jsonPath("$[0].date").value(date.toString()))
                .andExpect(jsonPath("$[0].isActive").value(reservationDate.isActive()));

            verify(reservationDateService).readDates();
        }
    }
}
