package whz.pti.eva.user.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import whz.pti.eva.common.domain.BaseEntity;
import whz.pti.eva.customer.domain.Customer;

/**
 * JPA-Entity-Klasse für Benutzer in der Pizza-Anwendung.
 * Speichert Login-Name, verschlüsseltes Passwort und Rolle.
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity<Long> {
    @Column(unique = true,  nullable = false)
    private String loginName;

    private String passwordHash;

    @Enumerated(value = EnumType.STRING)
    private Role role = Role.USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Customer customer;

    public User() {}

    public User(String loginName, String password) {
        this.loginName = loginName;
        this.passwordHash = password;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        if (customer != null) {
            customer.setUser(this);
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
