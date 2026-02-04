package com.devcast.fleetmanagement.features.company.dto;

import com.devcast.fleetmanagement.features.company.model.Client;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Client Response
 *
 * Returns client information to clients.
 * Includes all relevant details for displaying client data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponse {

    private Long id;

    private Long companyId;

    private String name;

    private String phone;

    private String email;

    private String address;

    private LocalDateTime createdAt;

    /**
     * Convert from Client entity to DTO
     */
    public static ClientResponse fromEntity(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyId(client.getCompany().getId())
                .name(client.getName())
                .phone(client.getPhone())
                .email(client.getEmail())
                .address(client.getAddress())
                .createdAt(client.getCreatedAt())
                .build();
    }

    /**
     * Convert from Client entity list to DTO list
     */
    public static java.util.List<ClientResponse> fromEntities(java.util.List<Client> clients) {
        return clients.stream()
                .map(ClientResponse::fromEntity)
                .toList();
    }
}
