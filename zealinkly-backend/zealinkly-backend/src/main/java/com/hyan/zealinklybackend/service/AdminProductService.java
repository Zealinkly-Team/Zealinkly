package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.ProductCreateRequest;
import com.hyan.zealinklybackend.dto.request.ProductUpdateRequest;
import com.hyan.zealinklybackend.dto.response.ProductResponse;
import com.hyan.zealinklybackend.entity.Product;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员商品管理服务
 */
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> listAll(Pageable pageable) {
        return productRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> listEnabled(Pageable pageable) {
        return productRepository.findByEnabledTrueOrderByCreatedAtDesc(pageable)
                .map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pointsPrice(request.getPointsPrice())
                .stock(request.getStock() != null ? request.getStock() : 0)
                .imageUrl(request.getImageUrl())
                .enabled(true)
                .build();
        product = productRepository.save(product);
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new BusinessException("商品不存在"));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPointsPrice() != null) {
            product.setPointsPrice(request.getPointsPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getEnabled() != null) {
            product.setEnabled(request.getEnabled());
        }

        product = productRepository.save(product);
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new BusinessException("商品不存在");
        }
        productRepository.deleteById(id);
    }
}
