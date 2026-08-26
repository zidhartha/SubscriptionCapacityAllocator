CREATE TABLE allocation_decision(
    decision_id BIGSERIAL PRIMARY KEY,
    request_id UUID NOT NULL,
    max_capacity INT NOT NULL,
    total_requested_amount INT NOT NULL,
    total_fee_revenue INT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX idx_allocation_decision_request_id on allocation_decision(request_id);

CREATE INDEX idx_allocation_decision_created_at
    ON allocation_decision (created_at DESC);

CREATE TABLE accepted_subscriptions(
    id BIGSERIAL PRIMARY KEY,
    investor_name VARCHAR(255) NOT NULL,
    requested_amount INT NOT NULL,
    fee_revenue INT NOT NULL,
    decision_id BIGINT NOT NULL,
    constraint fk_accepted_subscriptions_decision FOREIGN KEY(decision_id)
        REFERENCES allocation_decision(decision_id)
);

