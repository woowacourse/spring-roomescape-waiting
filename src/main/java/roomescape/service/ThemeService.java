package roomescape.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import roomescape.repository.ThemeRepository;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.exception.AlreadyInUseException;

@Service
public class ThemeService {
    private final String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/images/";
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        return themeRepository.findAllThemes()
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    public List<ThemeResponse> findTopTheme(Long count) {
        return themeRepository.findTopThemes(count)
                .stream()
                .map(ThemeResponse::from)
                .toList();
    }

    @Transactional
    public ThemeResponse create(ThemeRequest request) {
        MultipartFile file = request.file();
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + fileName;

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            file.transferTo(new File(filePath));

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장에 실패했습니다.", e);
        }

        String imageUrl = "/images/" + fileName;

        Theme theme = new Theme(
                request.name(),
                request.description(),
                imageUrl
        );

        Theme saved = themeRepository.save(theme);

        return ThemeResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (themeRepository.existsByThemeId(id)) {
            throw new AlreadyInUseException("해당 테마에 예약이 존재하여 삭제할 수 없습니다.");
        }
        themeRepository.delete(id);
    }
}
