package whz.pti.eva.favorite.boundary;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import whz.pti.eva.favorite.service.FavoritePizzaService;
import whz.pti.eva.user.domain.User;
import whz.pti.eva.user.service.currentuser.CurrentUserUtils;

import java.util.Optional;

@Controller
@RequestMapping("/favorites")
public class FavoritePizzaController {
    private final FavoritePizzaService favoriteService;

    public FavoritePizzaController(FavoritePizzaService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public String list(Model model, RedirectAttributes redirectAttributes) {
        Optional<User> user = CurrentUserUtils.getUser();
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um Favoriten zu sehen.");
            return "redirect:/login?redirect=/favorites";
        }
        model.addAttribute("favorites", favoriteService.getFavorites(user.get().getLoginName()));
        return "favorite/list";
    }

    @PostMapping("/{pizzaId}")
    public String add(@PathVariable Long pizzaId, RedirectAttributes redirectAttributes) {
        Optional<User> user = CurrentUserUtils.getUser();
        if (user.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bitte loggen Sie sich ein, um Favoriten zu speichern.");
            return "redirect:/login?redirect=/favorites";
        }
        favoriteService.add(user.get().getLoginName(), pizzaId);
        redirectAttributes.addFlashAttribute("success", "Pizza zu Favoriten hinzugefügt.");
        return "redirect:/favorites";
    }

    @PostMapping("/{pizzaId}/remove")
    public String remove(@PathVariable Long pizzaId) {
        CurrentUserUtils.getUser().ifPresent(user -> favoriteService.remove(user.getLoginName(), pizzaId));
        return "redirect:/favorites";
    }
}
