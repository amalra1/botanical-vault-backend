# Botanical Vault Backend

**Acervo de Plantas** is a private web app where an architect stores and finds the plant images (transparent PNGs) she uses in architectural collages. It replaces an unorganized local folder with a cloud library she can search, filter and organize by project.

The system has a single user (the architect). Login exists only to keep the library private; there is no sign-up, sharing or multi-tenancy.

The project is split into two repositories:

- **botanical-vault-backend** (this one): Java/Spring Boot REST API (data, auth, image storage).
- **botanical-vault-frontend**: React/Next.js front end.
