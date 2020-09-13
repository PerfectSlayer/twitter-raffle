# Paris JUG Twitter raffle website

This website is the Twitter raffle to gift our sponsor prizes.

<img src="resources/home.png" raw="true" alt="homepage">
<img src="resources/winner.png" raw="true" alt="showing a winner">

## Package

The application can be packaged as a jar application, the classic mode, or as a native application, the native mode.

### Classic Mode

Compile the project with Maven:

```shell
mvn package
```

### Native Mode

Compile project with Maven using native profile:

```shell
mvn package -Pnative
```

## Run

Run providing Twitter client credentials using CLI arguments:

```shell
# Run classic mode
java -Dtwitter4j.oauth.consumerKey=<consumerKey> \
  -Dtwitter4j.oauth.consumerSecret=<consumerSecret> \
  -Dtwitter4j.oauth.accessToken=<accessToken> \
  -Dtwitter4j.oauth.accessTokenSecret=<accessTokenSecret> \
  -jar target/quarkus-app/quarkus-run.jar
```

Run providing Twitter client credential using environment variables:

```shell
export twitter4j_oauth_consumerKey=<consumerKey>
export twitter4j_oauth_consumerSecret=<consumerSecret>
export twitter4j_oauth_accessToken=<accessToken>
export twitter4j_oauth_accessTokenSecret=<accessTokenSecret>
# Run classic mode
java -jar -jar target/quarkus-app/quarkus-run.jar
# Run native mode
target/twitter-raffle-1.x.y-runner
```
