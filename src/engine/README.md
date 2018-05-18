# Echo: Backend (Akka Edition)

This is a sample Scala project built with Gradle, using Akka.
 
- You can build it with ```./gradlew build```
- You can import it to IntelliJ IDEA
- It has ScalaStyle support
  
## Building fat Jar

```
gradle clean build shadowJar
```

### Docker

```
docker build -t echo-actors:latest .
```
