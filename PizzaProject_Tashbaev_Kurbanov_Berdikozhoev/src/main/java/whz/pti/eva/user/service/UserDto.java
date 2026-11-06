package whz.pti.eva.user.service;

import whz.pti.eva.user.domain.Role;

/**
 * Record für die Übertragung von Benutzerdaten in der Pizza-Anwendung.
 * Enthält ID, Login-Name und Rolle des Benutzers.
 */
public record UserDto(Long id, String loginName, Role role) {}

