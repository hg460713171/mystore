package com.h.system.mystore.infra.warehouse.resource;

import com.h.system.mystore.infra.infrastructure.jaxrs.CommonResponse;
import com.h.system.mystore.infra.warehouse.application.StockpileApplicationService;
import com.h.system.mystore.infra.domain.warehouse.DeliveredStatus;
import com.h.system.mystore.infra.domain.warehouse.Stockpile;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 库存相关的资源
 *
 * @author houjiahao
 * @date 2020/4/19 21:40
 **/

@Path("/products")
@Component
@CacheConfig(cacheNames = "resource.product")
@Produces(MediaType.APPLICATION_JSON)
public class StockpileResource {

    @Inject
    private StockpileApplicationService service;

    /**
     * 将指定的产品库存调整为指定数额
     */
    @PATCH
    @Path("/stockpile/{productId}")
    public Response updateStockpile(@PathParam("productId") Integer productId, @QueryParam("amount") Integer amount) {
        return CommonResponse.op(() -> service.setStockpileAmountByProductId(productId, amount));
    }

    /**
     * 根据产品查询库存
     */
    @GET
    @Path("/stockpile/{productId}")
    public Stockpile queryStockpile(@PathParam("productId") Integer productId) {
        return service.getStockpile(productId);
    }

    // 以下是开放给内部微服务调用的方法

    /**
     * 将指定的产品库存调整为指定数额
     */
    @PATCH
    @Path("/stockpile/delivered/{productId}")
    public Response setDeliveredStatus(@PathParam("productId") Integer productId, @QueryParam("status") DeliveredStatus status, @QueryParam("amount") Integer amount) {
        return CommonResponse.op(() -> service.setDeliveredStatus(productId, status, amount));
    }
}
