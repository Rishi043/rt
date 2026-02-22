✅ STEP 1 — Create Your Main Project Folder
Run this in Command Prompt:
cd C:\Users\rajve
mkdir vehicle-platform
cd vehicle-platform

✅ STEP 2 — Create Customer Service
Copy and paste this entire command (the ^ lets you write one command on multiple lines in Windows):
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.1:create ^
  -DprojectGroupId=com.rishi ^
  -DprojectArtifactId=customer-service ^
  -Dextensions="rest-jackson,jdbc-mysql,hibernate-orm-panache,smallrye-jwt,security"
Wait for it to finish. You'll see BUILD SUCCESS at the end.

✅ STEP 3 — Create Vehicle Service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.1:create ^
  -DprojectGroupId=com.rishi ^
  -DprojectArtifactId=vehicle-service ^
  -Dextensions="rest-jackson,jdbc-mysql,hibernate-orm-panache"

✅ STEP 4 — Create Order Service
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.1:create ^
  -DprojectGroupId=com.rishi ^
  -DprojectArtifactId=order-service ^
  -Dextensions="rest-jackson,jdbc-mysql,hibernate-orm-panache,rest-client-reactive"

✅ STEP 5 — Create API Gateway
mvn io.quarkus.platform:quarkus-maven-plugin:3.8.1:create ^
  -DprojectGroupId=com.rishi ^
  -DprojectArtifactId=api-gateway ^
  -Dextensions="rest,rest-client-reactive,smallrye-jwt"



CREATE DATABASE customer_db;
CREATE DATABASE vehicle_db;
CREATE DATABASE order_db;


A) customer-service — Add:

User.java entity
LoginRequest.java and LoginResponse.java DTOs
AuthService.java
AuthResource.java
Update application.properties

B) vehicle-service — Add:

Vehicle.java entity
VehicleDTO.java
VehicleService.java
VehicleResource.java
Update application.properties

C) order-service — Add:

OrderEntity.java
VehicleClient.java (REST client interface)
OrderService.java
Update application.properties

D) api-gateway — Add:

GatewayResource.java


✅ STEP 6 — Run the Services (Open 4 Terminal Windows)
Terminal 1 — Customer Service (runs on port 8081):
bashcd vehicle-platform/customer-service
./mvnw quarkus:dev
Terminal 2 — Vehicle Service (runs on port 8082):
bashcd vehicle-platform/vehicle-service
./mvnw quarkus:dev -Dquarkus.http.port=8082
Terminal 3 — Order Service (runs on port 8083):
bashcd vehicle-platform/order-service
./mvnw quarkus:dev -Dquarkus.http.port=8083
Terminal 4 — API Gateway (runs on port 8080):
bashcd vehicle-platform/api-gateway
./mvnw quarkus:dev -Dquarkus.http.port=8080

✅ STEP 7 — Test Your Services
Use Postman (free download at postman.com) or your browser to test:
ServiceURLMethodLoginhttp://localhost:8081/auth/loginPOSTList Vehicleshttp://localhost:8082/vehiclesGETAdd Vehiclehttp://localhost:8082/vehiclesPOSTPlace Orderhttp://localhost:8083/ordersPOST

----------------------------------



🔹 PROJECT STRUCTURE (Monorepo)
vehicle-platform/
│
├── api-gateway/
├── customer-service/
├── vehicle-service/
├── order-service/
└── docker-compose.yml
🚀 STEP 1 — CREATE PROJECTS

Run:

quarkus create app com.rishi:customer-service \
--extension='rest-jackson,jdbc-mysql,hibernate-orm-panache,smallrye-jwt,security'

quarkus create app com.rishi:vehicle-service \
--extension='rest-jackson,jdbc-mysql,hibernate-orm-panache'

quarkus create app com.rishi:order-service \
--extension='rest-jackson,jdbc-mysql,hibernate-orm-panache,rest-client'

quarkus create app com.rishi:api-gateway \
--extension='rest,rest-client,smallrye-jwt'
==========================================================
1️⃣ CUSTOMER SERVICE (Auth + Users)
==========================================================
📌 application.properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=root
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/customer_db

quarkus.hibernate-orm.database.generation=update

mp.jwt.verify.issuer=rishi-app
quarkus.smallrye-jwt.sign.key.location=privateKey.pem
📌 User Entity
package com.rishi.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;

@Entity
public class User extends PanacheEntity {

    @Column(unique = true)
    public String username;

    public String password;

    public String role; // DEALER / CUSTOMER
}
📌 DTOs
LoginRequest.java
package com.rishi.dto;

public class LoginRequest {
    public String username;
    public String password;
}
LoginResponse.java
package com.rishi.dto;

public class LoginResponse {
    public String token;

    public LoginResponse(String token) {
        this.token = token;
    }
}
📌 AuthService
package com.rishi.service;

import com.rishi.dto.LoginRequest;
import com.rishi.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    public String login(LoginRequest request) {

        User user = User.find("username", request.username).firstResult();

        if (user == null || !user.password.equals(request.password)) {
            throw new WebApplicationException("Invalid credentials", 401);
        }

        return Jwt.issuer("rishi-app")
                .subject(user.username)
                .groups(Set.of(user.role))
                .expiresIn(Duration.ofHours(2))
                .sign();
    }
}
📌 AuthResource
package com.rishi.resource;

import com.rishi.dto.LoginRequest;
import com.rishi.dto.LoginResponse;
import com.rishi.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService service;

    @POST
    @Path("/login")
    public LoginResponse login(LoginRequest request) {
        return new LoginResponse(service.login(request));
    }
}
==========================================================
2️⃣ VEHICLE SERVICE
==========================================================
application.properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=root
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/vehicle_db
quarkus.hibernate-orm.database.generation=update
Vehicle Entity
@Entity
public class Vehicle extends PanacheEntity {

    public String model;
    public String lineType;
    public String exteriorColor;
    public String interiorColor;
    public double basePrice;
}
VehicleDTO
public class VehicleDTO {
    public String model;
    public String lineType;
    public String exteriorColor;
    public String interiorColor;
    public double basePrice;
}
VehicleService
@ApplicationScoped
public class VehicleService {

    public Vehicle create(VehicleDTO dto) {
        Vehicle v = new Vehicle();
        v.model = dto.model;
        v.lineType = dto.lineType;
        v.exteriorColor = dto.exteriorColor;
        v.interiorColor = dto.interiorColor;
        v.basePrice = dto.basePrice;
        v.persist();
        return v;
    }

    public List<Vehicle> list() {
        return Vehicle.listAll();
    }
}
VehicleResource
@Path("/vehicles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleResource {

    @Inject
    VehicleService service;

    @POST
    public Vehicle create(VehicleDTO dto) {
        return service.create(dto);
    }

    @GET
    public List<Vehicle> list() {
        return service.list();
    }
}
==========================================================
3️⃣ ORDER SERVICE (With REST Client like Feign)
==========================================================
application.properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=root
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/order_db
quarkus.hibernate-orm.database.generation=update

quarkus.rest-client.vehicle-api.url=http://localhost:8082
REST Client Interface (Feign equivalent)
@RegisterRestClient(configKey="vehicle-api")
@Path("/vehicles")
public interface VehicleClient {

    @GET
    @Path("/{id}")
    Vehicle getById(@PathParam("id") Long id);
}
Order Entity
@Entity
public class OrderEntity extends PanacheEntity {

    public Long vehicleId;
    public double totalPrice;
    public String status;
    public LocalDateTime createdAt = LocalDateTime.now();
}
OrderService
@ApplicationScoped
public class OrderService {

    @Inject
    @RestClient
    VehicleClient vehicleClient;

    public OrderEntity placeOrder(Long vehicleId) {

        Vehicle vehicle = vehicleClient.getById(vehicleId);

        OrderEntity order = new OrderEntity();
        order.vehicleId = vehicleId;
        order.totalPrice = vehicle.basePrice;
        order.status = "CREATED";
        order.persist();

        return order;
    }
}
==========================================================
4️⃣ API GATEWAY (Quarkus Based)
==========================================================

Simple routing proxy.

You can create REST clients for each service and forward calls.

Example:

@Path("/gateway/orders")
public class GatewayResource {

    @Inject
    @RestClient
    OrderClient orderClient;

    @POST
    public OrderEntity placeOrder(Long vehicleId) {
        return orderClient.placeOrder(vehicleId);
    }
}
🗄 DATABASE SETUP
CREATE DATABASE customer_db;
CREATE DATABASE vehicle_db;
CREATE DATABASE order_db;
▶️ HOW TO RUN

Start MySQL

Run each service:

cd customer-service
./mvnw quarkus:dev

cd vehicle-service
./mvnw quarkus:dev -Dquarkus.http.port=8082

cd order-service
./mvnw quarkus:dev -Dquarkus.http.port=8083

cd api-gateway
./mvnw quarkus:dev -Dquarkus.http.port=8080
🔥 WHAT WE BUILT

✔ Clean DTO layer
✔ Service layer
✔ REST Client (Feign alternative)
✔ JWT authentication
✔ API Gateway
✔ Database per service
✔ Enterprise microservice structure