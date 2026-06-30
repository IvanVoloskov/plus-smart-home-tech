package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.dto.ProductCategory;
import ru.yandex.practicum.dto.ProductState;
import ru.yandex.practicum.dto.QuantityState;
import ru.yandex.practicum.model.SetProductQuantityStateRequest;
import ru.yandex.practicum.entity.ProductEntity;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductEntity> getProducts(ProductCategory category, Pageable pageable) {
        if (category == null) {
            throw new ProductNotFoundException("Категория не может быть null");
        }

        return productRepository.findByProductCategoryAndProductState(category, ProductState.ACTIVE, pageable);
    }

    public ProductEntity getProduct(UUID productId) {
        return productRepository.findById(productId).
                orElseThrow(() -> new ProductNotFoundException("Продукт не найден с ID: " + productId));
    }

    @Transactional
    public ProductEntity createProduct(ProductEntity product) {
        product.setProductId(null);
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Название товара не может быть пустым");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Цена должна быть больше нуля");
        }
        if (product.getProductCategory() == null) {
            throw new IllegalArgumentException("Категория товара не может быть null");
        }
        if (product.getQuantityState() == null) {
            product.setQuantityState(QuantityState.ENDED);
        }
        return productRepository.save(product);
    }

    @Transactional
    public ProductEntity updateProduct(ProductEntity updatedProduct) {
        ProductEntity exists = productRepository.findById(updatedProduct.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден с ID: " + updatedProduct.getProductId()));

        exists.setProductName(updatedProduct.getProductName());
        exists.setProductCategory(updatedProduct.getProductCategory());
        exists.setDescription(updatedProduct.getDescription());
        exists.setImageSrc(updatedProduct.getImageSrc());
        exists.setQuantityState(updatedProduct.getQuantityState());
        exists.setProductState(updatedProduct.getProductState());
        exists.setPrice(updatedProduct.getPrice());

        return productRepository.save(exists);
    }

    @Transactional
    public boolean removeProductFromStore(UUID productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден с ID: " + productId));

        product.setProductState(ProductState.DEACTIVATE);
        productRepository.save(product);
        return true;
    }

    @Transactional
    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден с ID: " + request.getProductId()));

        product.setQuantityState(request.getQuantityState());
        productRepository.save(product);
        return true;
    }
}
