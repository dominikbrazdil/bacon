# Bacon
Bacon is a new Java CLI for PNC 2.0 combining features of old PNC CLI and PiG tooling.


# Usage
Compile:
  mvn clean install

Run:
  java -jar cli/target/bacon.jar 

# Authentication

To authenticate to PNC Authentication servers, add this to your `config.yaml`:

```yaml
keycloak:
    url: "http://keycloak.com"
    realm: ""
    username: ""

    # if regular user
    clientId: ""
    password: ""

    # if service account
    # clientSecret: ""
```
