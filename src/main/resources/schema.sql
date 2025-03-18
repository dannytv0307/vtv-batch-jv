DROP TABLE IF EXISTS job_config;

CREATE TABLE IF NOT EXISTS job_config (
    id SERIAL PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN DEFAULT true,
    cron_expression VARCHAR(100) NOT NULL,
    last_run TIMESTAMP,
    next_run TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create pos_sale table
CREATE TABLE IF NOT EXISTS pos_sale (
    id SERIAL PRIMARY KEY,
    transaction_id BIGINT,
    date DATE,
    store_id VARCHAR(50),
    product_id VARCHAR(50),
    quantity INTEGER,
    unit_price DECIMAL(10,2),
    total_amount DECIMAL(10,2),
    state INTEGER DEFAULT 0,
    unikey VARCHAR(255),
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create pos_sale_summary table
CREATE TABLE IF NOT EXISTS pos_sale_summary (
    id SERIAL PRIMARY KEY,
    date DATE,
    store_code VARCHAR(50),
    product VARCHAR(50),
    quantity INTEGER,
    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial job configuration
INSERT INTO job_config (job_name, enabled, cron_expression, next_run)
VALUES ('posSaleJob', true, '0 20 5 * * ?', CASE 
    WHEN CURRENT_TIME < '05:20:00' THEN CURRENT_DATE + '05:20:00'::TIME 
    ELSE CURRENT_DATE + INTERVAL '1 day' + '05:20:00'::TIME 
END)
ON CONFLICT (job_name) DO UPDATE 
SET cron_expression = EXCLUDED.cron_expression,
    next_run = EXCLUDED.next_run; 