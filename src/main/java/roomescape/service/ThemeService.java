package roomescape.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import roomescape.config.UploadProperties;
import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequest;
import roomescape.dto.response.ThemeResponse;
import roomescape.repository.ThemeRepository;

@Service
@Transactional(readOnly = true)
public class ThemeService {
    private final String uploadDir;
    private final ThemeRepository themeRepository;

    public ThemeService(ThemeRepository themeRepository, UploadProperties uploadProperties) {
        this.uploadDir = uploadProperties.imagesDir();
        this.themeRepository = themeRepository;
    }

    public List<ThemeResponse> findAllThemes() {
        List<Theme> themes = themeRepository.findAllThemes();
        return themes.stream()
                .map(ThemeResponse::from)
                .toList();
    }

    /*public List<ThemeResponse> findTopTheme(Long count) {
        List<Theme> topTheme = themeRepository.findTopThemes(count);
        return topTheme.stream()
                .map(ThemeResponse::from)
                .toList();
    }*/

    @Transactional
    public ThemeResponse create(ThemeRequest request) {
        MultipartFile file = request.file();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File directory = new File(uploadDir);
        File target = Path.of(uploadDir, fileName).toFile();

        try {
            if (!directory.exists()) {
                directory.mkdirs();
            }
            file.transferTo(target);
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
        themeRepository.deleteById(id);
    }
}
