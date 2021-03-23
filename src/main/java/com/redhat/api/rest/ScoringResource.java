package com.redhat.api.rest;

import com.redhat.model.GameStatus;
import com.redhat.model.PlayerScore;
import io.quarkus.infinispan.client.Remote;
import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/scoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScoringResource {
   private static final Logger LOGGER = LoggerFactory.getLogger(ScoringResource.class.getName());

   @Inject
   @Remote(PlayerScore.PLAYERS_SCORES)
   RemoteCache<String, PlayerScore> playersScores;

   @Inject
   @Remote("players")
   RemoteCache<String, String> players;

   @GET
   public Response health() {
      int size = playersScores.size();
      LOGGER.info("Connected players size " + size);
      return Response.ok("Scoring Resource is ready. Cache players size " + size).build();
   }

   @POST
   @Path("/{gameId}/{matchId}/{userId}/win")
   public Response win(@PathParam("gameId") String gameId,
                       @PathParam("matchId") String matchId,
                       @PathParam("userId") String userId,
                       @QueryParam("timestamp") Long timestamp) {
      return handleGameOver(gameId, matchId, userId, timestamp, GameStatus.WIN);
   }

   @POST
   @Path("/{gameId}/{matchId}/{userId}/loss")
   public Response loss(@PathParam("gameId") String gameId,
                        @PathParam("matchId") String matchId,
                        @PathParam("userId") String userId,
                        @QueryParam("timestamp") Long timestamp) {
      return handleGameOver(gameId, matchId, userId, timestamp, GameStatus.LOSS);
   }

   @POST
   @Path("/{gameId}/{matchId}/{userId}")
   public Response score(@PathParam("gameId") String gameId,
                         @PathParam("matchId") String matchId,
                         @PathParam("userId") String userId,
                         @QueryParam("delta") int delta,
                         @QueryParam("human") boolean human,
                         @QueryParam("timestamp") long timestamp) {
      if(playersScores == null) {
         LOGGER.error("Unable score, players-score cache does not exist.");
      }

      String key = getKey(gameId, matchId, userId);

      PlayerScore playerScore = playersScores.get(key);

      if(playerScore == null) {
         String player = players != null ? players.get(userId) : "";
         String username = "";
         if (player != null && player != "") {
            // parse name
            username = new JsonObject(players.get(userId)).getString("username");
         }
         playerScore = new PlayerScore(userId, matchId, gameId, username, human, delta, timestamp, GameStatus.PLAYING);
      } else {
         playerScore.setScore(playerScore.getScore() + delta);
         playerScore.setTimestamp(timestamp);
      }

      playersScores.put(key, playerScore);
      return Response.accepted().build();
   }

   private Response handleGameOver(String gameId, String matchId, String userId, Long timestamp, GameStatus status) {
      if(playersScores == null) {
         LOGGER.error("Unable score, players-score cache does not exist.");
      }

      String key = getKey(gameId, matchId, userId);
      PlayerScore playerScore = playersScores.get(key);

      if(playerScore == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }

      if(playerScore.getGameStatus() != GameStatus.PLAYING) {
         return Response.status(Response.Status.BAD_REQUEST).build();
      }

      playerScore.setGameStatus(status);
      if(timestamp != null) {
         playerScore.setTimestamp(timestamp);
      }

      playersScores.put(key, playerScore);

      return Response.accepted().build();
   }

   private String getKey(String gameId, String matchId, String userId) {
      return gameId + '-' + matchId + '-' + userId;
   }
}
