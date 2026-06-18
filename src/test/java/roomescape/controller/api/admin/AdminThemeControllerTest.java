package roomescape.controller.api.admin;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.AlreadyExistsException;
import roomescape.service.ThemeService;

@WebMvcTest(AdminThemeController.class)
class AdminThemeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ThemeService themeService;

    @Test
    void 테마_생성() throws Exception {
        String name = "추리물";
        String description = "추리";
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png",
                "fake-image-content".getBytes());

        given(themeService.create(any())).willReturn(new ThemeResponse(1L, name, description, "http://image.url"));

        mockMvc.perform(multipart("/api/admin/themes")
                        .file(file)
                        .param("name", name)
                        .param("description", description)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, containsString("/api/admin/themes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(name));
    }

    @Test
    void 테마_삭제() throws Exception {
        willDoNothing().given(themeService).delete(16L);

        mockMvc.perform(delete("/api/admin/themes/16"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 예약이_존재하는_테마_삭제_불가() throws Exception {
        willThrow(new AlreadyExistsException("해당 테마에 예약이 존재하여 삭제할 수 없습니다."))
                .given(themeService).delete(1L);

        mockMvc.perform(delete("/api/admin/themes/1"))
                .andExpect(status().isConflict());
    }
}
