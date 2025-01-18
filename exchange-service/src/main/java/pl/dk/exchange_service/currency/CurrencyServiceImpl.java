package pl.dk.exchange_service.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.exchange_service.currency.dtos.CurrencyDto;
import pl.dk.exchange_service.exception.CurrencyNotFoundException;
import pl.dk.exchange_service.exception.ServerException;

import java.util.List;

@Service
@RequiredArgsConstructor
class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public CurrencyDto save(Currency currency) {
        Currency savedCurrency = currencyRepository.save(currency);
        return CurrencyDtoMapper.map(savedCurrency);
    }

    @Override
    public CurrencyDto getCurrencyById(String currencyId) {
        return currencyRepository.findById(currencyId)
                .map(CurrencyDtoMapper::map)
                .orElseThrow(() -> new CurrencyNotFoundException("Currency with id: %s not found".formatted(currencyId)));
    }

    @Override
    public List<CurrencyDto> getAllCurrencies(int page, int size) {
        --page;
        return currencyRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(CurrencyDtoMapper::map)
                .toList();
    }

    @Override
    @Transactional
    public void updateCurrency(String currencyId, JsonMergePatch jsonMergePatch) {
        CurrencyDto currencyDto = this.getCurrencyById(currencyId);
        try {
            CurrencyDto currencyDtoPatched = applyPatch(currencyDto, jsonMergePatch);
            Currency currencyToUpdate = CurrencyDtoMapper.map(currencyDtoPatched);
            currencyRepository.save(currencyToUpdate);
        } catch (JsonPatchException | JsonProcessingException e) {
            throw new ServerException("Server unavailable, please try again later");
        }
    }

    private CurrencyDto applyPatch(CurrencyDto currencyDto, JsonMergePatch jsonMergePatch)
            throws JsonPatchException, JsonProcessingException {
        JsonNode currencyNode = objectMapper.valueToTree(currencyDto);
        JsonNode apply = jsonMergePatch.apply(currencyNode);
        return objectMapper.treeToValue(apply, CurrencyDto.class);
    }

    @Override
    @Transactional
    public void deleteCurrencyById(String currencyId) {
        currencyRepository.findById(currencyId)
                .ifPresentOrElse(currencyRepository::delete, () -> {
                    throw new CurrencyNotFoundException("Currency with id: %s not found".formatted(currencyId));
                });
    }
}
