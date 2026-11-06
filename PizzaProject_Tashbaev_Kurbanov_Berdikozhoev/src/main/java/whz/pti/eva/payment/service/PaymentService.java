package whz.pti.eva.payment.service;

public interface PaymentService {

    void doActionPay(String loginName, double amount);

    void createAccount(String loginName);

    Double getBalance(String loginName);

    void deposit(String loginName, double amount);
}
