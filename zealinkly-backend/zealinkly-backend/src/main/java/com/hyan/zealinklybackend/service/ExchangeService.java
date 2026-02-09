package com.hyan.zealinklybackend.service;

import com.hyan.zealinklybackend.dto.request.ExchangeRequest;
import com.hyan.zealinklybackend.dto.response.ExchangeResponse;
import com.hyan.zealinklybackend.entity.*;
import com.hyan.zealinklybackend.exception.BusinessException;
import com.hyan.zealinklybackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 兑换服务
 */
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;
    private final ProductRepository productRepository;
    private final VolunteerRepository volunteerRepository;
    private final AdminRepository adminRepository;
    private final PointsLedgerRepository pointsLedgerRepository;

    @Transactional
    public ExchangeResponse exchange(Long adminId, ExchangeRequest request) {
        // 1. 验证志愿者
        Volunteer volunteer = volunteerRepository.findById(request.getVolunteerId())
                .orElseThrow(() -> new BusinessException("志愿者不存在"));
        
        if (!volunteer.getEnabled()) {
            throw new BusinessException("志愿者账号已禁用");
        }

        // 2. 验证商品
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException("商品不存在"));
        
        if (!product.getEnabled()) {
            throw new BusinessException("商品已下架");
        }

        // 3. 验证库存
        if (product.getStock() < request.getQuantity()) {
            throw new BusinessException("商品库存不足，当前库存：" + product.getStock());
        }

        // 4. 计算所需积分
        int totalCost = product.getPointsPrice() * request.getQuantity();

        // 5. 验证志愿者积分
        if (volunteer.getPoints() < totalCost) {
            throw new BusinessException("积分不足，需要 " + totalCost + " 积分，当前积分：" + volunteer.getPoints());
        }

        // 6. 获取管理员
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("管理员不存在"));

        // 7. 扣除积分
        volunteer.setPoints(volunteer.getPoints() - totalCost);
        volunteer = volunteerRepository.save(volunteer);

        // 8. 减少库存
        product.setStock(product.getStock() - request.getQuantity());
        product = productRepository.save(product);

        // 9. 创建兑换记录
        Exchange exchange = Exchange.builder()
                .volunteer(volunteer)
                .product(product)
                .quantity(request.getQuantity())
                .pointsCost(totalCost)
                .admin(admin)
                .build();
        exchange = exchangeRepository.save(exchange);

        // 10. 记录积分流水
        PointsLedger ledger = PointsLedger.builder()
                .userType("VOLUNTEER")
                .userId(volunteer.getId())
                .amount(-totalCost)
                .balanceAfter(volunteer.getPoints())
                .reason("GIFT_EXCHANGE")
                .exchange(exchange)
                .build();
        pointsLedgerRepository.save(ledger);

        // 11. 重新加载关联数据
        exchange = exchangeRepository.findByIdWithAssociations(exchange.getId())
                .orElse(exchange);

        return ExchangeResponse.fromEntity(exchange);
    }

    @Transactional(readOnly = true)
    public Page<ExchangeResponse> listAll(Pageable pageable) {
        return exchangeRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(exchange -> {
                    Exchange loaded = exchangeRepository.findByIdWithAssociations(exchange.getId())
                            .orElse(exchange);
                    return ExchangeResponse.fromEntity(loaded);
                });
    }

    @Transactional(readOnly = true)
    public ExchangeResponse getById(Long id) {
        Exchange exchange = exchangeRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new BusinessException("兑换记录不存在"));
        return ExchangeResponse.fromEntity(exchange);
    }
}
