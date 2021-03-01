# 2021-scoring-service

Scoring Service, baked with Quarkus and Infinispan

Run Infinispan with Docker

`docker run -v $(pwd):/user-config  -p 11222:11222 -e USER="admin" -e PASS="pass" infinispan/server:12.0.1.Final`

## Scoring REST API

Health : `http GET http://localhost:8080/scoring`

### Create or Update a score

` POST /scoring/g1/m1/u1?delta={delta}&human={human}&timestamp={timestamp}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that is scoring
* `delta`: increment of the score
* `human`: `true` for humans, `false` for AI
* `timestamp`: long milliseconds number 

Example: 
```bash 
http POST 'http://localhost:8080/scoring/g1/m1/u1?delta=123&human=true&timestamp=9090898'
```

### Track a win

`POST /scoring/{gameId}/{matchId}/{userId}/win?timestamp={timestamp}`

* `gameId`: Generation or id game
* `matchId`: the match id
* `userId`: player id that has won the match
* `timestamp`: long milliseconds number 

Example: 

```bash 
http POST 'http://localhost:8080/scoring/g1/m1/u1/win?timestamp=1223'
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
* `userId`: player id that has lost the match
* `timestamp`: long milliseconds number 
* `type`: shot type: `HIT`, `MISS` or `SUNK`
* `ship`: optional parameter. ship type `CARRIER` or `SUBMARINE`
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

```shell script
./run-app.sh
```
Access 
* Infinispan Console in `http://localhost:11222`. Log using admin/pass credentials
* Leaderboard: `http://localhost:8080`


`docker commit 93cee6062ce6 quay.io/redhatdemo/2021-scoring-service`
`docker push quay.io/redhatdemo/2021-scoring-service`