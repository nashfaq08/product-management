# product-management
Product Management Service

# This project is a microservices-based e-commerce system built using Spring Boot 3, providing:

- User Authentication & Authorization

- Product Management

- Order Management

- Dynamic Discount Calculation (Strategy Pattern)

- Inter-Service Communication via REST

- Caching with Redis (Optional, currently disabled)

Each service runs independently and communicates securely using JWT tokens.

# Key Architecture Points

JWT authentication is being handled by User Service

Product & Order services validate incoming JWT tokens

Order service communicates with Product service for:

  - stock validation
  - stock deduction
  - stock restoration

Discount calculation has been implemented with Strategy Pattern

Redis caching (for product reads) is implemented but disabled by default

# How to run the service

cd product-management (You can choose any service from the available ones)

mvn clean install

mvn spring-boot:run
