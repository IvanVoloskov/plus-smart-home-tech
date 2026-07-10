package ru.yandex.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.DeliveryDto;
import ru.yandex.practicum.entity.Address;
import ru.yandex.practicum.entity.DeliveryEntity;

@Component
public class DeliveryMapper {
    public DeliveryDto toDto(DeliveryEntity delivery) {

        if (delivery == null) {
            return null;
        }

        return DeliveryDto.builder()
                .deliveryId(delivery.getDeliveryId())
                .fromAddress(delivery.getFromAddress())
                .toAddress(delivery.getToAddress())
                .orderId(delivery.getOrderId())
                .deliveryState(delivery.getDeliveryState())
                .build();
    }

    public DeliveryEntity toEntity(DeliveryDto deliveryDto) {
        DeliveryEntity delivery = new DeliveryEntity();
        delivery.setDeliveryId(deliveryDto.getDeliveryId());
        delivery.setOrderId(deliveryDto.getOrderId());
        delivery.setDeliveryState(deliveryDto.getDeliveryState());
        delivery.setFromAddress(deliveryDto.getFromAddress());
        delivery.setToAddress(deliveryDto.getToAddress());
        return delivery;
    }

    public AddressDto toAddressDto(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDto.builder()
                .country(address.getCountry())
                .city(address.getCity())
                .street(address.getStreet())
                .house(address.getHouse())
                .flat(address.getFlat())
                .build();
    }

    public Address toAddress(AddressDto addressDto) {
        Address address = new Address();
        address.setCountry(addressDto.getCountry());
        address.setCity(addressDto.getCity());
        address.setStreet(addressDto.getStreet());
        address.setHouse(addressDto.getHouse());
        address.setFlat(addressDto.getFlat());
        return address;
    }
}
