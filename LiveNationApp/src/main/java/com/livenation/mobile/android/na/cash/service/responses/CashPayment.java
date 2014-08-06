package com.livenation.mobile.android.na.cash.service.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CashPayment extends CashResponse {
    public static final String ACTION_SEND = "SEND";
    public static final String ACTION_REQUEST = "REQUEST";

    public static final String STATE_WAITING_ON_SENDER = "WAITING_ON_SENDER";
    public static final String STATE_WAITING_ON_RECIPIENT = "WAITING_ON_RECIPIENT";
    public static final String STATE_PENDING = "PENDING";
    public static final String STATE_COMPLETED = "COMPLETED";
    public static final String STATE_CANCELED = "CANCELED";

    public static final String CANCELLATION_REASON_EXPIRED = "EXPIRED";
    public static final String CANCELLATION_REASON_SENDER_CANCELED = "SENDER_CANCELED";
    public static final String CANCELLATION_REASON_RECIPIENT_CANCELED = "RECIPIENT_CANCELED";
    public static final String CANCELLATION_REASON_SQUARE_CANCELED = "SQUARE_CANCELED";
    public static final String CANCELLATION_REASON_LIMIT_EXCEEDED = "LIMIT_EXCEEDED";
    public static final String CANCELLATION_REASON_DECLINED = "DECLINED";
    public static final String CANCELLATION_REASON_OTHER = "OTHER";

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("sender")
    private CashCustomer sender;

    @JsonProperty("recipient")
    private CashCustomer recipient;

    @JsonProperty("amount_money")
    private CashMoney amount;

    @JsonProperty("state")
    private String state;

    @JsonProperty("recipient_blockers")
    private CashPaymentBlockers recipientBlockers;

    @JsonProperty("sender_blockers")
    private CashPaymentBlockers senderBlockers;

    @JsonProperty("cancellation_reason")
    private String cancellationReason;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("captured_at")
    private String capturedAt;

    @JsonProperty("paid_out_at")
    private String paidOutAt;

    @JsonProperty("refunded_at")
    private String refundedAt;


    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public CashCustomer getSender() {
        return sender;
    }

    public void setSender(CashCustomer sender) {
        this.sender = sender;
    }

    public CashCustomer getRecipient() {
        return recipient;
    }

    public void setRecipient(CashCustomer recipient) {
        this.recipient = recipient;
    }

    public CashMoney getAmount() {
        return amount;
    }

    public void setAmount(CashMoney amount) {
        this.amount = amount;
    }

    public String getState() {
        return state;
    }

    public CashPaymentBlockers getRecipientBlockers() {
        return recipientBlockers;
    }

    public CashPaymentBlockers getSenderBlockers() {
        return senderBlockers;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCapturedAt() {
        return capturedAt;
    }

    public String getPaidOutAt() {
        return paidOutAt;
    }

    public String getRefundedAt() {
        return refundedAt;
    }


    @Override
    public String toString() {
        return "CashPayment{" +
                "paymentId='" + paymentId + '\'' +
                ", action='" + action + '\'' +
                ", sender=" + sender +
                ", recipient=" + recipient +
                ", amount=" + amount +
                ", state='" + state + '\'' +
                ", recipientBlockers=" + recipientBlockers +
                ", senderBlockers=" + senderBlockers +
                ", cancellationReason='" + cancellationReason + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", capturedAt='" + capturedAt + '\'' +
                ", paidOutAt='" + paidOutAt + '\'' +
                ", refundedAt='" + refundedAt + '\'' +
                '}';
    }
}
