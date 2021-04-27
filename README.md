# 2021-scoring-service

Scoring Service, baked with Quarkus and Infinispan

Run Infinispan with Docker

`docker run -v $(pwd):/user-config  -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0.1.Final`

## Scoring REST API

Health : `http GET http://localhost:8080/scoring`

### Create or Update a score

`POST /scoring/{gameId}}/{matchId}/{userId}?delta={delta}&username={username}&human={human}&timestamp={timestamp}&bonus={bonus}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that is scoring
* `username`: the player name, " " encoded as %20
* `delta`: increment of the score
* `human`: `true` for humans, `false` for AI
* `timestamp`: long milliseconds number 
* `bonus`: `true` for bonus scoring

Example: 
```bash 
http POST 'http://localhost:8080/scoring/g1/m1/u1?delta=123&username=pepe%20coco&human=true&timestamp=9090898'
```

### Get a score

`GET /scoring/{gameId}/{matchId}/{userId}/score`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that is scoring

Example:
```bash 
http GET 'http://localhost:8080/scoring/g1/m1/u1/score'
```

### Final ranking

`GET /scoring/{gameId}/ranking?max={max}`

* `gameId`: Generation or id game
* `max`: max results, default 1000

### Track a win

`POST /scoring/{gameId}/{matchId}/{userId}/win?timestamp={timestamp}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that has won the match
* `timestamp`: long milliseconds number 

Example: 

```bash 
http POST 'http://localhost:8080/scoring/{gameId}/{matchId}/{userId}/win?timestamp=1223'
```

### Track a loss

`POST /scoring/{gameId}/{matchId}/{userId}/loss?timestamp={timestamp}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that has lost the match
* `timestamp`: long milliseconds number

```bash 
http POST 'http://localhost:8080/scoring/g1/m1/u1/loss?timestamp=1223'
```

## Shots REST API

`POST /shot/{gameId}/{matchId}/{userId}/{timestamp}?type={type}&ship={ship}&human={human}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that has performed a shot in the match
* `timestamp`: long milliseconds number 
* `type`: shot type: `HIT`, `MISS` or `SUNK`
* `ship`: optional parameter. ship type `CARRIER`, `SUBMARINE`, `BATTLESHIP`, `DESTROYER`
* `human`: `true` for humans, `false` for AI

```bash 
http POST 'http://localhost:8080/shot/g1/m1/u1/10?type=MISS&ship=&human=true'

http POST 'http://localhost:8080/shot/g1/m1/u1/11?type=HIT&ship=CARRIER&human=true'

http POST 'http://localhost:8080/shot/g1/m1/u1/12?type=SUNK&ship=CARRIER&human=true'

```

## Run images (native or jvm)

1. Run Infinispan

```shell script
./run-infinispan.sh
```

2. Build following the instructions the native or the jvm image (instructions in Dockerfile or Dockerfile.jvm)

3. Run the application

TIP: Use env variable `SCORING_CONFIGURE_INFINISPAN=true` to create caches

```shell script
./run-app.sh
```
Access 
* Scoring: `http://localhost:8080`


`docker commit a358deaa1504 quay.io/redhatdemo/2021-scoring-service`
`docker push quay.io/redhatdemo/2021-scoring-service`
