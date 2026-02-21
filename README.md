# resilience4j-poc
We are  doing poc for learning resilience4j, and learn what problems can occur on production


📘 README — Section Before Circuit Breaker

You can add something like this.

🔹 Problem Statement

Modern microservices frequently depend on external systems such as:

Payment gateways

Risk engines

KYC services

Third-party APIs

If an external service becomes slow or unstable, it can:

Increase request latency

Block application threads

Cause cascading failures

Degrade overall system availability

This project demonstrates how to design a resilient external API integration layer using Spring Boot and Resilience4j.

🔹 Phase 1 – Simulating an Unstable External API

To reproduce real-world failure scenarios, a mock external API was implemented that:

Introduces artificial delay (800ms)

Fails randomly with 70% probability

This helps simulate:

Network instability

Temporary service outages

Latency spikes

Without resilience, failures propagate directly to the client.

🔹 Phase 2 – Implementing Retry Mechanism

Retry is implemented using Resilience4j’s @Retry annotation.

Configuration:

Max Attempts: 3

Wait Duration: 1 second

Retry on: RuntimeException

How It Works

If the external API fails:

The call is retried automatically.

A delay is introduced between attempts.

If all attempts fail, a fallback method is executed.

This provides:

Recovery from transient failures

Reduced immediate client-facing errors

Graceful degradation via fallback

🔹 Observations from Retry Implementation

From testing:

Successful recovery occurs if a later retry succeeds.

If all retries fail, fallback response is returned.

Response time increases proportionally with retry attempts.

Example:

If each call takes 800ms and there are 3 attempts with 1-second wait:

Worst-case latency ≈ 4–5 seconds.

🔹 Limitation of Retry

Retry alone is not sufficient in distributed systems.

If the external service is completely down:

Every request will retry multiple times.

Threads remain blocked during retries.

Latency increases significantly.

System throughput decreases.

Risk of thread pool exhaustion rises.

This can lead to cascading failures across microservices.

📘 Circuit Breaker Section (Add After Retry Section)
🔹 Phase 3 – Circuit Breaker Implementation

While Retry helps recover from transient failures, it is not sufficient when the downstream system is completely unavailable.

If the external service remains down:

Every incoming request will still retry.

Threads remain blocked during retry attempts.

Response latency increases significantly.

Risk of thread pool exhaustion rises.

Cascading failure may occur across microservices.

To prevent this, a Circuit Breaker pattern was implemented using Resilience4j.

🔹 Circuit Breaker Configuration
resilience4j.circuitbreaker.instances.externalApiCircuitBreaker.sliding-window-size=4
resilience4j.circuitbreaker.instances.externalApiCircuitBreaker.minimum-number-of-calls=4
resilience4j.circuitbreaker.instances.externalApiCircuitBreaker.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.externalApiCircuitBreaker.wait-duration-in-open-state=15s
resilience4j.circuitbreaker.instances.externalApiCircuitBreaker.permitted-number-of-calls-in-half-open-state=2
Configuration Explanation

Sliding Window Size – Number of recent calls considered for failure rate calculation.

Failure Rate Threshold – Percentage of failed calls required to open the circuit.

Wait Duration in Open State – Time circuit remains open before attempting recovery.

Permitted Calls in Half-Open State – Number of test calls allowed to check recovery.

🔹 Circuit Breaker States

The circuit breaker transitions between three states:

1️⃣ CLOSED (Healthy State)

All calls are allowed to reach the external API.

Failures are recorded.

If failure rate exceeds threshold → circuit moves to OPEN.

2️⃣ OPEN (Protection Mode)

Calls are NOT sent to the external API.

Fallback method is executed immediately.

Response time drops significantly.

System prevents resource exhaustion.

This avoids repeated unnecessary retries.

3️⃣ HALF-OPEN (Recovery Testing)

Limited test calls are allowed.

If test calls succeed → circuit moves to CLOSED.

If test calls fail → circuit returns to OPEN.

This allows graceful recovery without overwhelming the external system.

🔹 Observed Behavior During Testing

During testing:

Initial failures caused retries.

Once failure threshold was exceeded, circuit transitioned to OPEN.

Subsequent calls returned fallback immediately (no external call made).

After wait duration, circuit entered HALF-OPEN and allowed limited test calls.

This confirmed correct implementation of short-circuiting behavior.

🔹 Impact of Circuit Breaker

Adding Circuit Breaker resulted in:

Reduced latency when downstream system is unhealthy.

Immediate fallback response instead of delayed retries.

Prevention of cascading failures.

Improved system stability under failure conditions.

🔹 Architectural Insight

Retry handles transient failures.

Circuit Breaker handles systemic failures.

Together, they provide a layered resilience strategy for distributed systems.

🔥 Optional Advanced Section (Add If You Want To Impress)
🔹 Annotation Order Importance

When combining Retry and Circuit Breaker, annotation order matters.

The outer annotation wraps the inner one.

Example:

@CircuitBreaker(name = "externalApiCircuitBreaker", fallbackMethod = "fallback")
@Retry(name = "externalApiRetry")

CircuitBreaker should wrap Retry to ensure proper failure recording and state transition.