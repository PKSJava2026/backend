package com.beta.expedition.service;

import com.beta.expedition.exception.BusinessException;
import com.beta.expedition.exception.EntityNotFoundException;
import com.beta.expedition.model.CarrierContract;
import com.beta.expedition.model.CooperationRequest;
import com.beta.expedition.model.CooperationStatus;
import com.beta.expedition.repository.CarrierContractRepository;
import com.beta.expedition.repository.CooperationRequestRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class CooperationService {

    private static final int MAX_DESCRIPTION_LENGTH = 1000;
    private static final int MAX_VEHICLE_LENGTH = 500;

    private final CooperationRequestRepository requests;
    private final ContractService<CarrierContract> carrierContracts;
    private final CarrierContractRepository carrierContractRepository;

    public CooperationService(CooperationRequestRepository requests,
                              ContractService<CarrierContract> carrierContracts,
                              CarrierContractRepository carrierContractRepository) {
        this.requests = requests;
        this.carrierContracts = carrierContracts;
        this.carrierContractRepository = carrierContractRepository;
    }

    public CooperationRequest create(long carrierId, String description, String vehicleInfo) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Расскажите о себе: описание обязательно");
        }
        if (vehicleInfo == null || vehicleInfo.isBlank()) {
            throw new BusinessException("Информация о транспорте обязательна");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH || vehicleInfo.length() > MAX_VEHICLE_LENGTH) {
            throw new BusinessException("Описание не длиннее " + MAX_DESCRIPTION_LENGTH
                    + " символов, транспорт не длиннее " + MAX_VEHICLE_LENGTH);
        }
        requests.findByCarrierId(carrierId).stream()
                .filter(r -> r.getStatus() == CooperationStatus.NEW)
                .findFirst()
                .ifPresent(r -> {
                    throw new BusinessException("У вас уже есть необработанная заявка #" + r.getId());
                });
        CooperationRequest request = new CooperationRequest();
        request.setCarrierId(carrierId);
        request.setDescription(description.trim());
        request.setVehicleInfo(vehicleInfo.trim());
        return requests.save(request);
    }

    public List<CooperationRequest> listByCarrier(long carrierId) {
        return requests.findByCarrierId(carrierId);
    }

    public List<CooperationRequest> listNew() {
        return requests.findByStatus(CooperationStatus.NEW);
    }

    public void cancel(long carrierId, long requestId) {
        CooperationRequest request = requests.findById(requestId)
                .filter(r -> r.getCarrierId() == carrierId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка о сотрудничестве", requestId));
        requireNew(request);
        requests.updateStatus(requestId, CooperationStatus.CANCELLED);
    }

    public CarrierContract createContract(long forwarderId, long requestId, long orderId, BigDecimal price,
                                          String terms, LocalDate startDate, LocalDate endDate) {
        CooperationRequest request = requests.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка о сотрудничестве", requestId));
        requireNew(request);
        CarrierContract contract = carrierContracts.create(forwarderId, orderId, request.getCarrierId(),
                price, terms, startDate, endDate);
        carrierContractRepository.linkCooperation(contract.getId(), requestId);
        contract.setCooperationId(requestId);
        requests.updateStatus(requestId, CooperationStatus.PROCESSED);
        return contract;
    }

    private void requireNew(CooperationRequest request) {
        if (request.getStatus() != CooperationStatus.NEW) {
            throw new BusinessException("Заявка #" + request.getId() + " уже обработана (" + request.getStatus() + ")");
        }
    }
}
