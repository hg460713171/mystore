package com.h.system.mystore.infra.paymnet.mock;

import com.h.system.mystore.infra.domain.warehouse.DeliveredStatus;
import com.h.system.mystore.infra.domain.warehouse.Product;
import com.h.system.mystore.infra.domain.warehouse.Stockpile;
import com.h.system.mystore.infra.paymnet.domain.client.ProductServiceClient;
import org.springframework.context.annotation.Primary;

import javax.inject.Named;

/**
 * @author houjiahao
 * @date 2020/4/21 17:07
 **/
@Named
@Primary
public class ProductServiceClientMock implements ProductServiceClient {

    @Override
    public Product getProduct(Integer id) {
        if (id == 1) {
            Product product = new Product();
            product.setId(1);
            product.setTitle("深入理解Java虚拟机（第3版）");
            product.setPrice(129d);
            product.setRate(9.6f);
            return product;
        } else {
            return null;
        }
    }

    @Override
    public Product[] getProducts() {
        return new Product[]{getProduct(1)};
    }

    @Override
    public void setDeliveredStatus(Integer productId, DeliveredStatus status, Integer amount) {
    }

    @Override
    public Stockpile queryStockpile(Integer productId) {
        if (productId == 1) {
            Stockpile stock = new Stockpile();
            stock.setId(1);
            stock.setAmount(30);
            stock.setProduct(getProduct(1));
            return stock;
        } else {
            return null;
        }
    }

}
