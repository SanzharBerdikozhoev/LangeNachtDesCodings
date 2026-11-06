package whz.pti.eva.customer.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import whz.pti.eva.user.domain.User;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.deliveryAddresses WHERE c.user = :user")
    Customer findByUserWithAddresses(@Param("user") User user);
}
