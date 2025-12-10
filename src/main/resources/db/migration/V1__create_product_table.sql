-- V1__create_product_table.sql

CREATE TABLE product (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(255) NOT NULL,
     description TEXT,
     price DOUBLE PRECISION,
     quantity INT,
     deleted BOOLEAN DEFAULT FALSE NOT NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
