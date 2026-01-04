package com.rxbuddy.common.messaging;

public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE_NAME = "rxbuddy.events";

    // Routing Keys
    public static final String ROUTING_KEY_BILL_CREATED = "billing.bill.created";
    public static final String ROUTING_KEY_BILL_CANCELLED = "billing.bill.cancelled";
    public static final String ROUTING_KEY_STOCK_DEDUCTED = "inventory.stock.deducted";
    public static final String ROUTING_KEY_STOCK_LOW = "inventory.stock.low";
    public static final String ROUTING_KEY_USER_CREATED = "user.user.created";
    public static final String ROUTING_KEY_TENANT_CREATED = "tenant.tenant.created";
    public static final String ROUTING_KEY_SUBSCRIPTION_EXPIRED = "tenant.subscription.expired";

    // Queues
    public static final String QUEUE_STOCK_DEDUCT = "inventory.stock.deduct";
    public static final String QUEUE_STOCK_RESTORE = "inventory.stock.restore";
    public static final String QUEUE_WALLET_CREDIT = "customer.wallet.credit";
    public static final String QUEUE_COMMISSION_CREATE = "doctor.commission.create";
    public static final String QUEUE_NOTIFICATION_SEND = "notification.send";
}
