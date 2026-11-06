package whz.pti.eva.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import whz.pti.eva.cart.service.CartService;

import java.io.IOException;

/**
 * Custom AuthenticationSuccessHandler, der nach erfolgreichem Login
 * ruft Merging des anonymen Warenkorbs mit dem Benutzer-Warenkorb auf und
 * Weiterleitung des Benutzers zur gewünschten Seite oder zur Startseite
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final CartService cartService;

    public LoginSuccessHandler(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Wird nach erfolgreicher Benutzerauthentifizierung aufgerufen.
     * <p>
     * Ablauf:
     * <ol>
     *     <li>Benutzer-ID aus der Authentication extrahieren</li>
     *     <li>Anonyme und registrierte Warenkörbe zusammenführen</li>
     *     <li>Redirect</li>
     * </ol>
     *
     * @param request        HTTP-Request, enthält Cookies und evtl. eine redirect-Query-Parameter
     * @param response       HTTP-Response, wird benutzt um Cookies zu löschen oder Redirect auszuführen
     * @param authentication Objekt mit den Authentifizierungsdaten des Benutzers
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String loginName = authentication.getName();
        cartService.mergeSessionCartIntoDb(loginName);

        String redirectUrl = (String) request.getSession().getAttribute("LOGIN_REDIRECT");

        try {
            if (redirectUrl != null) {
                request.getSession().removeAttribute("LOGIN_REDIRECT");
                response.sendRedirect(redirectUrl);
            } else {
                response.sendRedirect("/");
            }
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        }
    }
}
