SmartOrder — Business Requirements Document
Confidential
BUSINESS REQUIREMENTS DOCUMENT
SmartOrder
Multi-Vendor E-Commerce Marketplace Platform
Document Version: 1.0
Status: Draft for Stakeholder Review
Classification: Internal
Prepared for: SmartOrder Project Stakeholders
Page 1 of 11
SmartOrder — Business Requirements Document Confidential
Page 2 of 11
Table of Contents
Table of Contents ....................................................................................................................... 2
Document Control ...................................................................................................................... 3
Revision History ..................................................................................................................... 3
Distribution List ....................................................................................................................... 3
1. Executive Summary ............................................................................................................... 4
2. Business Objectives ............................................................................................................... 4
3. Project Scope ......................................................................................................................... 4
   3.1 In-Scope Capabilities (Delivered) ..................................................................................... 4
   3.2 Out-of-Scope for Current Revision .................................................................................... 5
4. Stakeholders & User Roles .................................................................................................... 5
5. Functional Requirements ....................................................................................................... 6
   5.1 Authentication & User Management ................................................................................. 6
   5.2 Product Catalog & Search ................................................................................................ 6
   5.3 Shopping Cart .................................................................................................................. 7
   5.4 Seller Dashboard .............................................................................................................. 7
   5.5 Notifications & Audit ......................................................................................................... 8
6. Non-Functional Requirements ................................................................................................ 8
   6.1 Architecture & Maintainability ............................................................................................ 8
   6.2 Security ............................................................................................................................ 8
   6.3 Performance & Scalability ................................................................................................. 8
   6.4 Reliability & Observability ................................................................................................. 9
   6.5 Compliance & Data Retention ........................................................................................... 9
7. System Architecture Overview ............................................................................................... 9
   7.1 Backend Microservices ..................................................................................................... 9
   7.2 Frontend Application ........................................................................................................10
   7.3 Cross-Cutting Infrastructure .............................................................................................10
8. Assumptions, Dependencies & Constraints ...........................................................................10
   8.1 Assumptions ....................................................................................................................10
   8.2 Dependencies .................................................................................................................10
   8.3 Constraints ......................................................................................................................10
9. Acceptance Criteria & Success Metrics .................................................................................10
10. Glossary ..............................................................................................................................11


SmartOrder — Business Requirements Document
Confidential
Document Control
Revision History
Version
Date
Author
Description
0.1
Project Kickoff
Product / Engineering
1.0
Phase 1–5
Completion
Initial draft based on
architectural blueprint
Product / Engineering
Updated to reflect
implemented scope across
infrastructure, auth, catalog,
cart, and seller modules
Distribution List
• Product Owner
• Engineering Lead / Solution Architect
• Backend Development Team
• Frontend Development Team
• QA / Test Lead
• DevOps / Infrastructure
Page 3 of 11
SmartOrder — Business Requirements Document
Confidential
1. Executive Summary
   SmartOrder is a production-oriented, multi-vendor e-commerce marketplace that connects
   independent sellers with customers through a unified storefront. The platform is being built as a
   cloud-native system using a Java/Spring Boot microservices backend and a Next.js server
   rendered frontend, with each business capability (authentication, catalog, cart, notifications)
   isolated into an independently deployable service.
   The system follows Hexagonal (Ports and Adapters) Architecture throughout the backend,
   ensuring that core business logic remains free of framework dependencies and is independently
   testable. This BRD documents the business objectives, functional scope, and non-functional
   requirements for the platform as defined and delivered across five development phases:
   infrastructure setup, authentication, product catalog and search, cart and checkout foundation,
   and seller/notification capabilities.
   This document is intended to align business stakeholders, product management, and the
   engineering team on what has been built, what it does, and the standards it is held to. It is a
   living document and will be revised as the Order/Checkout Saga, Payment processing, and
   Admin Control Panel phases are completed.
2. Business Objectives
   The SmartOrder platform is designed to achieve the following business outcomes:
   • Enable independent sellers to list, manage, and sell products through a self-service
   onboarding and product management experience.
   • Provide customers with a fast, reliable, and SEO-optimized shopping experience with
   real-time search and filtering.
   • Support secure, role-based access for four distinct user types: Customers, Sellers,
   Administrators, and Support agents.
   • Establish a resilient, horizontally scalable architecture capable of handling independent
   scaling of catalog, cart, and order workloads.
   • Maintain a complete audit trail of account and product lifecycle events for compliance
   and customer support purposes.
   • Minimize cart abandonment through persistent, cross-device shopping carts for both
   registered and guest users.
   • Provide a foundation that supports future expansion into multi-currency, multi-region,
   and third-party payment integrations.
3. Project Scope
   3.1 In-Scope Capabilities (Delivered)
   The following capabilities have been designed and implemented as of this revision:
   Page 4 of 11
   SmartOrder — Business Requirements Document Confidential
   Page 5 of 11
   Module Capability
   Platform Infrastructure Centralized configuration management, service discovery, API gateway
   with routing, rate limiting, and CORS handling
   Authentication & Identity Registration, login, JWT access/refresh token issuance, account
   lockout, password change, role-based access control
   Product Catalog Product creation, editing, image upload, category management, seller
   approval workflow, full-text search, autocomplete
   Shopping Cart Guest and authenticated persistent carts, item management, cart
   merge on login
   Seller Experience Seller dashboard, product management UI, image upload workflow,
   submission for admin review
   Notifications & Audit Event-driven email notifications and platform-wide audit logging for
   account and product events

3.2 Out-of-Scope for Current Revision
The following capabilities are part of the broader platform vision but are not covered by this
revision of the BRD and remain in subsequent phases:
• Order placement, Saga-based checkout orchestration, and compensation logic
• Payment gateway integration (Stripe / PayPal abstraction)
• Coupon and promotion calculation engine
• Seller analytics and revenue reporting dashboards
• Admin control panel for platform-wide moderation
• Automated integration and contract testing (Testcontainers suite)
• Multi-currency and multi-region localization
4. Stakeholders & User Roles
   The platform defines four distinct roles, each with a dedicated experience and permission set
   enforced at both the API Gateway and individual service level.
   Role Description Primary Capabilities
   Customer End consumer browsing and
   purchasing products
   Browse catalog, search, manage cart,
   maintain account profile
   Seller Independent merchant listing
   products for sale
   Create/edit/delete products, upload images,
   submit listings for review, view own product
   status
   Admin Platform administrator Approve/reject product listings, manage user
   accounts, full platform oversight
   SmartOrder — Business Requirements Document Confidential
   Page 6 of 11
   Role Description Primary Capabilities
   Support Customer support agent Read-only access to account and order
   information for support resolution
5. Functional Requirements
   5.1 Authentication & User Management
   ID Requirement Priority
   FR-AUTH-01 The system shall allow customers and sellers to self-register with
   email, password, first name, and last name. Must Have
   FR-AUTH-02 The system shall prevent self-assignment of ADMIN or SUPPORT
   roles during registration. Must Have
   FR-AUTH-03 The system shall enforce password complexity: minimum 8
   characters, at least one uppercase letter, and one digit. Must Have
   FR-AUTH-04 The system shall issue a short-lived JWT access token and a longer
   lived rotating refresh token upon successful login. Must Have
   FR-AUTH-05 The system shall lock a user account for 30 minutes after 5
   consecutive failed login attempts. Must Have
   FR-AUTH-06 The system shall allow authenticated users to change their password,
   which revokes all existing sessions. Must Have
   FR-AUTH-07 The system shall support logout from a single device or all devices. Should
   Have
   FR-AUTH-08 The system shall soft-delete user accounts, anonymizing the email
   while retaining records for audit purposes.
   Should
   Have

5.2 Product Catalog & Search
ID Requirement Priority
FR-CAT-01 The system shall allow sellers to create products as drafts with name,
description, price, category, brand, SKU, and tags. Must Have
FR-CAT-02 The system shall require at least one product image before a product
can be submitted for review. Must Have
FR-CAT-03 The system shall route new product submissions through an Admin
approval workflow (Draft → Pending → Active/Rejected). Must Have
FR-CAT-04 The system shall support a configurable rejection reason visible to the
seller. Must Have
SmartOrder — Business Requirements Document Confidential
Page 7 of 11
ID Requirement Priority
FR-CAT-05 The system shall provide full-text search across product name, brand,
tags, and description with relevance ranking. Must Have
FR-CAT-06 The system shall provide autocomplete suggestions as the customer
types in the search bar.
Should
Have
FR-CAT-07 The system shall support filtering search results by category and price
range, with sorting by price, rating, or recency. Must Have
FR-CAT-08 The system shall support up to 10 images per product, limited to
JPEG, PNG, and WebP formats under 5MB each. Must Have
FR-CAT-09 The system shall generate unique, URL-friendly slugs for SEO
optimized product pages.
Should
Have

5.3 Shopping Cart
ID Requirement Priority
FR-CART-01 The system shall allow guest users (unauthenticated) to maintain a
persistent shopping cart identified by a client-generated token. Must Have
FR-CART-02 The system shall automatically merge a guest cart into the
authenticated user's cart immediately upon login. Must Have
FR-CART-03 The system shall allow customers to add, update quantity, and remove
individual cart items. Must Have
FR-CART-04 The system shall cap individual item quantity at 99 units and limit a
cart to 50 distinct products.
Should
Have
FR-CART-05 Authenticated user carts shall persist for 30 days; guest carts shall
persist for 7 days. Must Have
FR-CART-06 The system shall calculate cart subtotal, discount, and total in real
time. Must Have

5.4 Seller Dashboard
ID Requirement Priority
FR-SELL-01 The system shall provide sellers with a dashboard summarizing
product count, active orders, and revenue. Must Have
FR-SELL-02 The system shall provide a product management table filterable by
listing status (Draft, Pending, Active, Rejected). Must Have
FR-SELL-03 The system shall allow sellers to upload product images directly from
the product creation form with live preview. Must Have
SmartOrder — Business Requirements Document
Confidential
ID
Requirement
Priority
FR-SELL-04
The system shall allow sellers to submit draft or rejected products for
admin review with a single action.
5.5 Notifications & Audit
Must Have
ID
Requirement
Priority
FR-NOTIF-01 The system shall send a welcome email upon successful registration.
Should
Have
FR-NOTIF-02
The system shall send a security notification email when an account is
locked or a password is changed.
FR-NOTIF-03 The system shall persist an immutable audit log entry for every
Must Have
account and product lifecycle event.
Must Have
FR-NOTIF-04
The system shall record the delivery status (sent/failed) of every
notification attempt for support troubleshooting.
Should
Have
6. Non-Functional Requirements
   6.1 Architecture & Maintainability
   • The backend shall follow Hexagonal (Ports and Adapters) Architecture, with domain
   logic fully decoupled from Spring Framework annotations.
   • Each microservice shall own its own datastore (database-per-service pattern); no
   service shall directly query another service's database.
   • All inter-service identity propagation shall occur via signed JWTs validated once at the
   API Gateway, with decoded claims forwarded as trusted internal headers.
   6.2 Security
   • Passwords shall be hashed using BCrypt with a minimum cost factor of 12.
   • All cross-service traffic for protected resources shall require a valid JWT issued by the
   Authentication Service.
   • The API Gateway shall enforce IP-based and user-based rate limiting, with stricter limits
   applied to authentication endpoints to mitigate brute-force attacks.
   • All error responses shall follow a uniform, non-leaking format that avoids exposing
   internal system details.
   6.3 Performance & Scalability
   • Each microservice shall be independently horizontally scalable behind the API Gateway
   and service registry.
   Page 8 of 11
   SmartOrder — Business Requirements Document
   Confidential
   • Product search shall be served from a dedicated Elasticsearch index rather than direct
   relational queries to ensure sub-second response times at scale.
   • Shopping cart reads/writes shall be served from Redis to support high-frequency, low
   latency cart operations.
   6.4 Reliability & Observability
   • All services shall expose health, metrics, and readiness endpoints for orchestration and
   monitoring.
   • All requests shall propagate a correlation ID across service boundaries to support
   distributed tracing and incident investigation.
   • Domain events (e.g., product approved, account locked) shall be published
   asynchronously via Kafka, decoupling core transactional flows from downstream side
   effects such as email delivery.
   6.5 Compliance & Data Retention
   • User account deletions shall be implemented as soft deletes with email anonymization,
   preserving historical order and audit integrity.
   • All security-relevant events (login, lockout, password change) shall be retained in an
   immutable audit log.
7. System Architecture Overview
   SmartOrder is implemented as a Gradle multi-module monorepo containing independently
   deployable Spring Boot microservices, fronted by a Next.js 14 application. The platform runs on
   Java 21 and is fully containerized via Docker Compose for local and staging environments.
   7.1 Backend Microservices
   Service
   Responsibility
   Primary Datastore
   Config Server
   Centralized externalized configuration for all
   services
   Filesystem / Git
   backed
   Eureka Server
   Service discovery and registry
   API Gateway
   In-memory
   Request routing, JWT validation, rate limiting, CORS Redis (rate limiting)
   Auth Service
   Identity, authentication, RBAC, session
   management
   Product Service
   PostgreSQL, Redis
   Catalog, categories, image management, search
   indexing
   PostgreSQL,
   Elasticsearch, MinIO
   Cart Service
   Guest and authenticated shopping cart persistence
   Notification Service
   Redis
   Event-driven email delivery and audit logging
   MongoDB
   Page 9 of 11
   SmartOrder — Business Requirements Document
   Confidential
   7.2 Frontend Application
   The customer- and seller-facing web application is built with Next.js 14 using the App Router,
   with server-side rendering for SEO-critical product and search pages. State management uses
   Zustand for authentication and cart state, with React Hook Form and Zod for form validation. All
   API traffic is routed exclusively through the API Gateway.
   7.3 Cross-Cutting Infrastructure
   • Apache Kafka: asynchronous domain event distribution between services
   • Redis: distributed caching, shopping cart storage, refresh token storage, gateway rate
   limiting
   • Elasticsearch: full-text product search and autocomplete
   • MinIO: S3-compatible object storage for product images
   • MongoDB: notification delivery logs and platform audit trail
   • Zipkin: distributed tracing across service boundaries
8. Assumptions, Dependencies & Constraints
   8.1 Assumptions
   • Sellers are responsible for the accuracy of product listings; the platform performs
   structural validation only, not content moderation beyond the Admin approval gate.
   • Initial deployment targets a single-region environment; multi-region support is a future
   consideration.
   • Email delivery in non-production environments uses a sandboxed SMTP relay (e.g.,
   Mailtrap) and is not delivered to real customer inboxes.
   8.2 Dependencies
   • Availability of PostgreSQL, Redis, Kafka, Elasticsearch, MongoDB, and MinIO as
   supporting infrastructure (provisioned via Docker Compose for this revision).
   • A future Payment Service Provider integration (e.g., Stripe) is required before the
   Order/Checkout phase can be considered functionally complete.
   8.3 Constraints
   • All services target Java 21 LTS and Spring Boot 3.2.5 for backend consistency.
   • The frontend targets Next.js 14 with the App Router; pages requiring SEO must be
   server-rendered.
   • All monetary values are currently modeled in a single currency per transaction (multi
   currency cart support is not yet implemented).
9. Acceptance Criteria & Success Metrics
   Page 10 of 11
   SmartOrder — Business Requirements Document
   Confidential
   The following criteria define readiness for stakeholder sign-off on the scope covered by this
   document:
1. A new user can register, receive a welcome notification event, and log in to obtain a
   valid access and refresh token pair.
2. A seller can create a draft product, upload at least one image, submit it for review, and
   have it approved or rejected by an Admin via the API.
3. A customer can search for a product by keyword, apply price and category filters, and
   view a fully rendered, SEO-optimized product detail page.
4. A guest user can add items to a cart, and upon logging in, see those items automatically
   merged into their authenticated cart.
5. All account and product lifecycle events produce a corresponding, queryable audit log
   entry within the Notification Service.
6. All services register successfully with the Eureka service registry and report healthy
   status via their actuator health endpoints.
   Success Metrics (post-launch, indicative targets for future measurement):
   • 95th percentile product search response time under 500ms
   • Cart operation success rate above 99.5%
   • Account lockout false-positive rate below 0.1% of legitimate login attempts
10. Glossary
    Term
    Definition
    Hexagonal Architecture
    An architectural pattern that isolates core business logic behind ports
    (interfaces), with adapters implementing technical concerns (REST,
    database, messaging)
    JWT
    JSON Web Token — a signed, stateless token used to convey
    authenticated user identity and claims
    Saga Pattern
    A pattern for managing distributed transactions across microservices via
    a sequence of local transactions and compensating actions
    RBAC
    Role-Based Access Control — authorization model restricting actions
    based on assigned user role
    SSR
    Server-Side Rendering — rendering page content on the server before
    delivery to the browser, improving SEO and initial load performance
    Outbox Pattern
    A reliability pattern where domain events are written to a local database
    table within the same transaction as the business operation, then
    asynchronously published
    Page 11 of 11 