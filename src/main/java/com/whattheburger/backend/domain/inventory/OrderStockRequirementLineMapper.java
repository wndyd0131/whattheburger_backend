package com.whattheburger.backend.domain.inventory;

import com.whattheburger.backend.domain.enums.CountType;
import com.whattheburger.backend.domain.order.Order;
import com.whattheburger.backend.domain.order.OrderCustomRule;
import com.whattheburger.backend.domain.order.OrderProduct;
import com.whattheburger.backend.domain.order.OrderProductOption;
import com.whattheburger.backend.domain.order.QuantityDetail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OrderStockRequirementLineMapper {

    private OrderStockRequirementLineMapper() {
    }

    public static List<StockRequirementLine> fromOrder(Order order) {
        List<StockRequirementLine> lines = new ArrayList<>();

        for (OrderProduct orderProduct : order.getOrderProducts()) {
            int productQuantity = orderProduct.getQuantity();
            for (OrderCustomRule orderCustomRule : orderProduct.getOrderCustomRules()) {
                for (OrderProductOption orderProductOption : orderCustomRule.getOrderProductOptions()) {
                    CountType countType = orderProductOption.getCountType();
                    if (countType == null || countType == CountType.NONE) {
                        continue;
                    }

                    lines.add(new StockRequirementLine(
                            productQuantity,
                            countType,
                            orderProductOption.getProductOptionId(),
                            orderProductOption.getQuantity(),
                            countType == CountType.UNCOUNTABLE && orderProductOption.getQuantityDetail() != null
                                    ? orderProductOption.getQuantityDetail().getProductOptionOptionQuantityId()
                                    : null
                    ));
                }
            }
        }

        return lines;
    }

    public static Set<Long> collectCountableProductOptionIds(List<StockRequirementLine> lines) {
        Set<Long> ids = new HashSet<>();
        for (StockRequirementLine line : lines) {
            if (line.countType() == CountType.COUNTABLE) {
                ids.add(line.productOptionId());
            }
        }
        return ids;
    }

    public static Set<Long> collectUncountablePooqIds(List<StockRequirementLine> lines) {
        Set<Long> ids = new HashSet<>();
        for (StockRequirementLine line : lines) {
            if (line.countType() == CountType.UNCOUNTABLE && line.pooqId() != null) {
                ids.add(line.pooqId());
            }
        }
        return ids;
    }
}
