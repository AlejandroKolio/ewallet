#### E-Wallet Service based on Vert.x.

Is a RESTful API for money transfers between accounts.
Service includes internal currency rates and convertation between currencies.
Just for simplicity, all the currency rates are static (data from 21.08.2019) 
and never changing.

#### Building

To build the project using verify or install goal to run unit and IT tests:

```
mvn clean install
```

#### Testing

The application is tested using [vertx-unit5](https://vertx.io/docs/vertx-junit5/java/).

#### Packaging

The application is packaged as a _fat jar_, using the 
[Vert.x fabric8 Maven Plugin](hhttps://vmp.fabric8.io/#packaging).

#### Running

Once packaged, just launch the _fat jar_ as follows:

```
java -jar target/ewallet-1.0-SNAPSHOT.jar
```

Then, open a browser to http://localhost:8080/api.

> FYI: openapi documentation is provided and can be found in [here](https://github.com/AlejandroKolio/ewallet/blob/master/src/main/resources/openapi.yml) 

#### Requirements
- Java 8
- Maven 3.3.9 or greater
#### Explicit requirements:
     1. You can use Java or Kotlin.
     2. Keep it simple and to the point (e.g. no need to implement any authentication).
     3. Assume the API is invoked by multiple systems and services on behalf of end users.
     4. You can use frameworks/libraries if you like (except Spring), but don't forget about
     requirement #2 and keep it simple and avoid heavy frameworks.
     5. The datastore should run in-memory for the sake of this test.
     6. The final result should be executable as a standalone program (should not require a
     pre-installed container/server).
     7. Demonstrate with tests that the API works as expected.
     Implicit requirements:
     1. The code produced by you is expected to be of high quality.
     2. There are no detailed requirements, use common sense.
     Please put your work on github or bitbucket.
