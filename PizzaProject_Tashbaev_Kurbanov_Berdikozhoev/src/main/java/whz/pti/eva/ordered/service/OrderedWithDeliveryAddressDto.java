package whz.pti.eva.ordered.service;

import whz.pti.eva.ordered.domain.Ordered;

public record OrderedWithDeliveryAddressDto(Ordered order, String deliveryAddress) { }
