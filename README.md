# PowerNest

## Abstract
PowerNest offers people a fast and easy way to charge their electric cars, allowing them to make advance reservations for charging stations, manage those reservations, and even enjoy the flexibility of finding stations available for immediate use.

## Changes after presentation
* ### Integration tests with cucumber
* ### Functional continuous delivery
* ### Load tests
* ### Xray setup completion
* ### Compose health status
* ### API documentation
* ### Product Specification Report
* ### Quality Manual

## The Team
* **Product Owner​**: 113682​ - Gabriel Santos
* **QA Engineer**: 113893​ - Guilherme Santos
* **Architecture**: 114514​ - João Gaspar
* **Team coordinator**: 115697 - Shelton Agostinho

## Architecture Diagram
![Architecture](docs/architecture/Architecture.png)

## Database Models and Class Diagram

*Database Models*

![Database Model](docs/architecture/Database%20Model.png)

*Class Diagram*

![Class Diagram](docs/architecture/ClassDiagram.png)

## API Documentation

The API documentation can be accessed at [http://deti-tqs-09.ua.pt:8080/swagger-ui/index.html](http://deti-tqs-09.ua.pt:8080/swagger-ui/index.html) if you are on the UA network, or at [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) if you run it locally.

---

*Screenshot*

![API Documentation Screenshot](docs/api.png)


## Running the Project
### Accessing the Production Environment
Our application is available at [http://deti-tqs-09.ua.pt/](http://deti-tqs-09.ua.pt/)  
*(Accessible only through the UA network or via VPN)*

### Running Locally
*Clone the project*
```bash
$ git clone git@github.com:GCapaross/TQS-Group-Project.git
```
*Change to the project root*
```bash
$ cd TQS-Group-Project
```
*Ensure the .env file is present, then:*
```bash
$ docker compose up -d --build
```
*You can access the site at [localhost:5173/](http://localhost:5173/)*
