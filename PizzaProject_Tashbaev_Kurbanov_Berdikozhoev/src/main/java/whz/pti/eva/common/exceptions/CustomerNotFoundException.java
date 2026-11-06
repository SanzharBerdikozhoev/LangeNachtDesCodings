package whz.pti.eva.common.exceptions;

public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long id) {
        super("Customer mit id " + id + " wurde nicht gefunden.");
    }
}
