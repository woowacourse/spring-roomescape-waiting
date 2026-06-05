package roomescape.theme.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.theme.application.port.in.CreateThemeUseCase;
import roomescape.theme.application.port.in.DeleteThemeUseCase;
import roomescape.theme.application.port.in.FindThemeUseCase;
import roomescape.theme.application.dto.request.ThemeSaveRequest;
import roomescape.theme.application.dto.response.ThemeFindResponse;
import roomescape.theme.application.dto.response.ThemeSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager/themes")
@RequiredArgsConstructor
public class ManagerThemeController {
    private final CreateThemeUseCase createThemeUseCase;
    private final FindThemeUseCase findThemeUseCase;
    private final DeleteThemeUseCase deleteThemeUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<ThemeSaveResponse>> save(
            @RequestBody @Valid ThemeSaveRequest body
    ) {
        ThemeSaveResponse response = createThemeUseCase.save(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ThemeFindResponse>>> findAll() {
        List<ThemeFindResponse> responses = findThemeUseCase.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable @Positive long id
    ) {
        deleteThemeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}
