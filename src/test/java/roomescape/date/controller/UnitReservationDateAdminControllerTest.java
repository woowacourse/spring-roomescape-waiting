package roomescape.date.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static roomescape.date.exception.ReservationDateErrorInformation.DATE_ALREADY_EXISTS;

@WebMvcTest(ReservationDateAdminController.class)
class UnitReservationDateAdminControllerTest {

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

    @Test
    @DisplayName("날짜를 정상적으로 등록하면, 등록한 날짜 정보를 반환한다.")
    void create() throws Exception {
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
    @DisplayName("날짜등록시 date가 null인지 검증한다.")
    void create_date_input_null() throws Exception {
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
    @DisplayName("중복 날짜 예외 발생시, 에러 응답 바디가 스펙대로 반환된다.")
    void create_already_exists() throws Exception {
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

    @Test
    @DisplayName("날짜 상태를 변경하면, 변경한 날짜 정보를 반환한다.")
    void updateStatus() throws Exception {
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
    @DisplayName("날짜_상태_변경시_isActive가_null인지 검증한다.")
    void updateStatus_isActive_input_null() throws Exception {
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

    @Test
    @DisplayName("날짜 목록을 조회하면, 날짜 목록을 반환한다.")
    void getReservationDates() throws Exception {
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
