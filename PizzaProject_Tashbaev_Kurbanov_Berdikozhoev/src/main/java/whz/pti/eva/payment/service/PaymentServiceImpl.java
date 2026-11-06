package whz.pti.eva.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import whz.pti.eva.common.exceptions.PaymentFailedException;
import whz.pti.eva.payment.service.dto.AccountResponseDTO;
import whz.pti.eva.payment.service.dto.TransferDTO;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${service.payment.url}")
    private String paymentUrl;

    @Value("${service.payment.plainCreds}")
    private String plainCreds;

    private final RestClient restClient = RestClient.create();

    @Override
    public void doActionPay(String loginName, double amount) {

        String authHeader = buildBasicAuthHeader();
        String url = paymentUrl + loginName + "/payment";

        Map<String, Object> body = Map.of("amount", amount);

        try {
            restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientResponseException ex) {
            String msg = "Zahlung fehlgeschlagen";

            try {
                Map<String, Object> error = new ObjectMapper().readValue(ex.getResponseBodyAsString(), Map.class);

                if (error.containsKey("message")) {
                    msg = error.get("message").toString();
                }
            } catch (Exception ignore) {
            }

            throw new PaymentFailedException(msg);
        }
    }

    @Override
    public void createAccount(String loginName) {

        String authHeader = buildBasicAuthHeader();

        String url = paymentUrl + loginName;

        restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public Double getBalance(String loginName) {
        String url = paymentUrl + loginName + "/account";
        AccountResponseDTO response = restClient.get()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader())
                .retrieve()
                .body(AccountResponseDTO.class);
        return response.balance();
    }


    @Override
    public void deposit(String loginName, double amount) {
        String url = paymentUrl + loginName + "/deposit";
        TransferDTO dto = new TransferDTO(amount);

        try {
            restClient.patch()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException rcrex) {
            String msg = "Einzahlung fehlgeschlagen";
            try {
                Map<?, ?> error = new ObjectMapper().readValue(rcrex.getResponseBodyAsString(), Map.class);
                if (error.containsKey("message")) msg = error.get("message").toString();
            } catch (Exception ignore) {
            }
            throw new PaymentFailedException(msg);
        }
    }


    private String buildBasicAuthHeader() {
        String encoded = Base64.getEncoder().encodeToString(plainCreds.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
