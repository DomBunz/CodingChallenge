-- Create audit logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP NOT NULL,
    username VARCHAR(255) NOT NULL,
    controller VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_data TEXT,
    response_status INTEGER,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    execution_time_ms BIGINT
);

-- Create indexes for better query performance
CREATE INDEX idx_audit_logs_username ON audit_logs(username);
CREATE INDEX idx_audit_logs_controller ON audit_logs(controller);
CREATE INDEX idx_audit_logs_endpoint ON audit_logs(endpoint);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);

-- Add comment to table
COMMENT ON TABLE audit_logs IS 'Stores audit logs for all API requests';
