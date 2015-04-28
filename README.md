# Apache Camel Demo Application
### Overview

This web application implements a web-service that provide methods to upload and download data associated with certain 'stores'. Uploaded data is put into queue and will be cached. Get requests receive data from cache. There is 'general' store that provide data which is appended to each response.

### Briefly about implementation

-   **Apache Camel** - main application framework, responsible for integration of all components;
-   **Spring DSL** for route configuration;
-   **Apache CXF** as WS implementation;
-   **ActiveMQ** - JMS broker;
-   **Ehcache** - Camel Cache implementation;
-   Lifecycle support by **Maven**;
-   Unit test (**jUnit** which start embedded **Jetty** for tests)

### Environment

Application was created with:
+  JDK 8u40
+  Tomcat 8.0.* 

### Tech stack

+  Apache Camel 2.15
+  Spring 4.1 (Context)
+  Apache ActiveMQ 5.10
+  Apache CXF 3
+  Jackson 2.5.1 (REST JSON processing)
+  JUnit4 + Jetty 9.2       

### Deployment

Lifecycle tasks are performed by Apache Maven.
Application can be deployed with *tomcat7-maven-plugin* as WAR archive.

Properties (broker connection, logging, etc.):
+  resources/app.properties
+  resources/log4j.properties
