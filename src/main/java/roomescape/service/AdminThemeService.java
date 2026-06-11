package roomescape.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import roomescape.domain.repository.ThemeRepository;
import roomescape.dto.ThemeRequest;
import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

@Service
public class AdminThemeService {

    private final ThemeRepository themeRepository;

    public AdminThemeService(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    public Long save(ThemeRequest request) {
        try {
            return themeRepository.save(request.name(), request.description(), request.thumbnailUrl());
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.ALREADY_EXISTS_THEME);
        }
    }

    public void delete(long id) {
        try {
            themeRepository.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.UNALLOWED_DELETE_EXISTS_THEME);
        }
    }
}
