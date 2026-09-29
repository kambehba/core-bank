RESTful API for a Financial Institution
Authentication & Authorization
Implemented stateless authentication using JWT (JJWT 0.12+) with Spring Security
Designed system to avoid server-side sessions (no session storage, fully token-based)
Issued JWT tokens containing user identity and role claims after successful login

Request Processing
Built custom JwtAuthenticationFilter (OncePerRequestFilter) to:
Extract JWT from request headers
Validate token signature and expiration
Populate Spring SecurityContext with authenticated user
Eliminated per-request database lookups by relying on token claims

Authorization (RBAC)
Implemented Role-Based Access Control (RBAC) using embedded JWT roles
Secured endpoints via:
URL-level rules (SecurityFilterChain)
Method-level annotations (@PreAuthorize)
Enabled fine-grained access control (e.g., ADMIN vs USER)

Security Best Practices
Used BCrypt hashing for secure password storage
Ensured token integrity via signed JWTs
Enforced authentication + authorization on all protected endpoints

Key Benefits
Stateless → highly scalable architecture
No session or DB dependency per request → improved performance
Clean separation of concerns → maintainable and secure design
