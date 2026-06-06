package roomescape.controller.theme;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.controller.theme.dto.ThemeResponse;
import roomescape.exception.ApiException;
import roomescape.exception.ErrorCode;
import roomescape.service.theme.ThemeService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/pages/admin/themes")
public class ThemeAdminPageController {

    private final ThemeService themeService;

    public ThemeAdminPageController(final ThemeService themeService) {
        this.themeService = themeService;
    }

    @GetMapping
    public String getThemeAdminPage(
            @RequestParam(required = false) final String errorCode,
            final Model model
    ) {
        model.addAttribute("themes", themeService.getAll().stream()
                .map(ThemeResponse::from)
                .toList());
        model.addAttribute("errorCode", errorCode);
        return "theme/list";
    }

    @PostMapping
    public String createTheme(
            @RequestParam(required = false) final String name,
            @RequestParam(required = false) final String description,
            @RequestParam(required = false) final String thumbnailUrl,
            final RedirectAttributes redirectAttributes
    ) {
        try {
            themeService.save(name, description, thumbnailUrl);
        } catch (ApiException exception) {
            redirectAttributes.addAttribute("errorCode", exception.getCode());
            return "redirect:/pages/admin/themes";
        } catch (Exception exception) {
            redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            return "redirect:/pages/admin/themes";
        }

        return "redirect:/pages/admin/themes";
    }

    @PostMapping("/{id}/delete")
    public String deleteTheme(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        try {
            themeService.deleteById(id);
        } catch (ApiException exception) {
            redirectAttributes.addAttribute("errorCode", exception.getCode());
            return "redirect:/pages/admin/themes";
        } catch (Exception exception) {
            redirectAttributes.addAttribute("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
            return "redirect:/pages/admin/themes";
        }

        return "redirect:/pages/admin/themes";
    }
}
