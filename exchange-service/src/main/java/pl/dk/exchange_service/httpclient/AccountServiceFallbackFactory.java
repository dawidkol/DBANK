package pl.dk.exchange_service.httpclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pl.dk.exchange_service.exception.AccountServiceUnavailable;
import pl.dk.exchange_service.exception.ExchangeServiceUnavailable;

@Component
class AccountServiceFallbackFactory implements FallbackFactory<AccountServiceFeignClient> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AccountServiceFeignClient create(Throwable cause) {
        if (cause instanceof FeignException.FeignClientException) {
            // Extract the JSON part of the response
            String message = cause.getMessage();
            String jsonPart = message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1);

            // Parse JSON
            JsonNode jsonNode = null;
            try {
                jsonNode = objectMapper.readTree(jsonPart);
            } catch (JsonProcessingException e) {
                throw new ExchangeServiceUnavailable("Internal server error, try again later");
            }
            // Extract "message" field
            String msg = jsonNode.get("message").asText();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
        throw new AccountServiceUnavailable(cause.getMessage());
    }
}
