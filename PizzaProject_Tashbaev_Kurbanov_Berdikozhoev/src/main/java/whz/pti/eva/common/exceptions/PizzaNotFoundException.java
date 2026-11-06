package whz.pti.eva.common.exceptions;

public class PizzaNotFoundException extends RuntimeException {
    public PizzaNotFoundException(Long id) {
        super("Pizza mit id " + id + " wurde nicht gefunden.");
    }
}
