package pl.dk.loanservice.httpClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pl.dk.loanservice.exception.LoanServiceUnavailable;
import pl.dk.loanservice.exception.TransferServiceUnavailableException;

@Component
class TransferServiceFallbackFactory implements FallbackFactory<TransferServiceFeignClient> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public TransferServiceFeignClient create(Throwable cause) {
        if (cause instanceof FeignException.BadRequest) {
            // Extract the JSON part of the response
            String message = cause.getMessage();
            String jsonPart = message.substring(message.indexOf("{"), message.lastIndexOf("}") + 1);

            // Parse JSON
            JsonNode jsonNode = null;
            try {
                jsonNode = objectMapper.readTree(jsonPart);
            } catch (JsonProcessingException e) {
                throw new LoanServiceUnavailable();
            }
            // Extract "message" field
            String msg = jsonNode.get("message").asText();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }
        throw new TransferServiceUnavailableException(cause.getMessage());
    }
}
