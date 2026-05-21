package roomescape.controller.view;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.provider.AuthorizationExtractor;
import roomescape.provider.JwtProvider;
import roomescape.repository.BranchRepository;
import roomescape.repository.UserRepository;

@ControllerAdvice(basePackages = "roomescape.controller.view")
public class LoginInfoAdvice {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;

    public LoginInfoAdvice(JwtProvider jwtProvider, UserRepository userRepository, BranchRepository branchRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
    }

    @ModelAttribute
    public void addLoginInfo(HttpServletRequest request, Model model) {
        User user = resolveUserOrNull(request);
        Role role = null;
        if (user != null) {
            role = user.role();
        }
        model.addAttribute("isStaff", role == Role.ADMIN || role == Role.MANAGER);
        model.addAttribute("isAdmin", role == Role.ADMIN);
        model.addAttribute("isManager", role == Role.MANAGER);
        if (role == Role.MANAGER) {
            branchRepository.findByManagerId(user.id())
                    .ifPresent(branch -> model.addAttribute("branchName", branch.name()));
        }
    }

    private User resolveUserOrNull(HttpServletRequest request) {
        try {
            String token = AuthorizationExtractor.extract(request);
            long id = jwtProvider.getId(token);
            return userRepository.findById(id).orElse(null);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
