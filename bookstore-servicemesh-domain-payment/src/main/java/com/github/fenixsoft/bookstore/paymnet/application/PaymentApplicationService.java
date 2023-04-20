/*
 * Copyright 2012-2020. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. More information from:
 *
 *        https://github.com/fenixsoft
 */

package com.github.fenixsoft.bookstore.paymnet.application;

import com.github.fenixsoft.bookstore.paymnet.domain.Payment;
import com.github.fenixsoft.bookstore.paymnet.domain.client.ProductServiceClient;
import com.github.fenixsoft.bookstore.paymnet.domain.service.PaymentService;
import com.github.fenixsoft.bookstore.paymnet.domain.service.WalletService;
import com.github.fenixsoft.bookstore.dto.Settlement;
import org.springframework.cache.Cache;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * 支付应用务
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 16:29
 **/
@Named
@Transactional
public class PaymentApplicationService {

    @Inject
    private PaymentService paymentService;

    @Inject
    private WalletService walletService;

    @Inject
    private ProductServiceClient productService;

    @Resource(name = "settlement")
    private Cache settlementCache;

    /**
     * 根据结算清单的内容执行，生成对应的支付单
     */
    public Payment executeBySettlement(Settlement bill) {
        // 从服务中获取商品的价格，计算要支付的总价（安全原因，这个不能由客户端传上来）
        productService.replenishProductInformation(bill);
        // 冻结部分库存（保证有货提供）,生成付款单
        Payment payment = paymentService.producePayment(bill);
        // 设立解冻定时器（超时未支付则释放冻结的库存和资金）
        paymentService.setupAutoThawedTrigger(payment);
        return payment;
    }

    /**
     * 完成支付
     * 立即取消解冻定时器，执行扣减库存和资金
     */
    public void accomplishPayment(Integer accountId, String payId) {
        // 订单从冻结状态变为派送状态，扣减库存
        double price = paymentService.accomplish(payId);
        try {
            // 扣减货款
            walletService.decrease(accountId, price);
        } catch (Throwable e) {
            // 扣款失败，将已支付的记录回滚恢复库存
            paymentService.rollbackSettlement(Payment.State.PAYED, payId);
            throw e;
        }
        // 支付成功的清除缓存
        settlementCache.evict(payId);
    }

    /**
     * 取消支付
     * 立即触发解冻定时器，释放库存和资金
     */
    public void cancelPayment(String payId) {
        // 释放冻结的库存
        paymentService.cancel(payId);
        // 支付成功的清除缓存
        settlementCache.evict(payId);
    }

}
