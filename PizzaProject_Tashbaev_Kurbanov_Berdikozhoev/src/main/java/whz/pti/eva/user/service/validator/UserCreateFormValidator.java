package whz.pti.eva.user.service.validator;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import whz.pti.eva.user.domain.UserCreateForm;
import org.springframework.validation.Validator;
import whz.pti.eva.user.service.user.UserService;

/**
 * Validator für das UserCreateForm in der Registrierung der Pizza-Anwendung.
 * Überprüft Passwort-Wiederholung und Einzigartigkeit des Login-Namens.
 */
@Component
public class UserCreateFormValidator implements Validator {
  private final UserService userService;

  /**
   * Konstruktor, der den UserService injiziert.
   * 
   * @param userService Service für Benutzeroperationen
   */
  public UserCreateFormValidator(UserService userService) {
    this.userService = userService;
  }

  /**
   * Überprüft, ob der Validator UserCreateForm unterstützt.
   * 
   * @param clazz Klasse zum Überprüfen
   * @return true, falls UserCreateForm
   */
  @Override
  public boolean supports(Class<?> clazz) {
    return clazz.equals(UserCreateForm.class);
  }

  /**
   * Führt die Validierung des Formulars durch.
   * 
   * @param target Das zu validierende Objekt
   * @param errors Fehler-Container
   */
  @Override
  public void validate(Object target, Errors errors) {
    UserCreateForm form = (UserCreateForm) target;
    validatePasswords(errors, form);
    validateLoginName(errors, form);
  }

  /**
   * Validiert, ob Passwörter übereinstimmen.
   * 
   * @param errors Fehler-Container
   * @param form   Das Formular
   */
  private void validatePasswords(Errors errors, UserCreateForm form) {
    if (!form.getPassword().equals(form.getPasswordRepeated())) {
      errors.reject("password", "Unterschiedliche Passwörter eingegeben! Vertippt?");
    }
  }

  /**
   * Validiert, ob der Login-Name bereits existiert.
   * 
   * @param errors Fehler-Container
   * @param form   Das Formular
   */
  private void validateLoginName(Errors errors, UserCreateForm form) {
    if (userService.existsByLoginName(form.getLoginName())) {
      errors.reject("loginName", "Nutzer mit diesem Loginname existiert bereits !!!");
    }
  }
}
