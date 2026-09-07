package com.example.bookingsystem.service;

import com.example.bookingsystem.common.exception.NotFoundException;
import com.example.bookingsystem.service.dto.CreateServiceRequest;
import com.example.bookingsystem.service.dto.ServiceResponse;
import com.example.bookingsystem.service.dto.UpdateServiceRequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


@Service
class ServiceService {
    private final ServiceRepository repository;

    public ServiceService(
            ServiceRepository serviceRepository
    ) {
        repository = serviceRepository;
    }

    public Page<ServiceResponse> getAll(Pageable pageable) {
        return repository
                .findAll(pageable)
                .map(ServiceMapper::toResponse);
    }

    public ServiceResponse getById(Long id) {
        ServiceEntity service = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Service", id));

        return ServiceMapper.toResponse(service);
    }

    public ServiceResponse create(CreateServiceRequest request) {
        ServiceEntity service = new ServiceEntity(
                request.name(),
                request.description(),
                request.durationMinutes(),
                request.price()
        );

        ServiceEntity savedEntity = repository.save(service);

        return ServiceMapper.toResponse(savedEntity);
    }

    @Transactional
    public ServiceResponse update(Long id, UpdateServiceRequest request) {
        ServiceEntity service = repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Service", id));

        service.setName(request.name());
        service.setDescription(request.description());
        service.setDurationMinutes(request.durationMinutes());
        service.setPrice(request.price());

        ServiceEntity savedService = repository.save(service);
        return ServiceMapper.toResponse(savedService);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
