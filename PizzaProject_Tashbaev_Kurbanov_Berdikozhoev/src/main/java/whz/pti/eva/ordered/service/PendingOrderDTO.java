package whz.pti.eva.ordered.service;

import java.util.List;

public class PendingOrderDTO {

    private List<Long> sessionIndexes;

    private Long deliveryAddressId;

    public PendingOrderDTO(List<Long> itemIds, Long deliveryAddressId) {
        this.sessionIndexes = itemIds;
        this.deliveryAddressId = deliveryAddressId;
    }

    public List<Long> getSessionIndexes() {
        return sessionIndexes;
    }

    @Override
    public String toString() {
        return "PendingOrderDTO{" +
                "sessionIndexes=" + sessionIndexes +
                ", deliveryAddressId=" + deliveryAddressId +
                '}';
    }
}
