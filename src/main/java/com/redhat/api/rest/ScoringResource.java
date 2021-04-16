package com.redhat.api.rest;

import com.redhat.model.GameStatus;
import com.redhat.model.PlayerScore;
import io.quarkus.infinispan.client.Remote;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
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
import java.util.List;

@Path("/scoring")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class ScoringResource {
   private static final Logger LOGGER = LoggerFactory.getLogger(ScoringResource.class.getName());

   @Inject
   @Remote(PlayerScore.PLAYERS_SCORES)
   RemoteCache<String, PlayerScore> playersScores;

   @GET
   public Response health() {
      if(playersScores == null) {
         LOGGER.error("players-score cache does not exist.");
         return Response.ok("players-score cache does not exist.").build();
      }
      int size = playersScores.size();
      LOGGER.info("Connected players size " + size);
      return Response.ok("Scoring Resource is ready. Cache players size " + size).build();
   }

   @POST
   @Path("/{gameId}/{matchId}/{userId}")
   public Response score(@PathParam("gameId") String gameId,
                         @PathParam("matchId") String matchId,
                         @PathParam("userId") String userId,
                         @QueryParam("delta") int delta,
                         @QueryParam("human") boolean human,
                         @QueryParam("timestamp") long timestamp,
                         @QueryParam("bonus") Boolean bonus,
                         @QueryParam("username") String username) {
      Response response = scoringServiceError();
      if (response != null) {
         return response;
      }

      String key = getKey(gameId, matchId, userId);

      PlayerScore playerScore = playersScores.get(key);

      if(playerScore == null) {
         // username
         String usernameDecoded = username != null ? username.replaceAll("%20", " ") : "";
         playerScore = new PlayerScore(userId, matchId, gameId, usernameDecoded, human, delta, timestamp, GameStatus.PLAYING, 0);
      } else {
         playerScore.setScore(playerScore.getScore() + delta);
         playerScore.setTimestamp(timestamp);
      }

      if(bonus!= null && bonus.booleanValue()) {
         playerScore.setBonus(playerScore.getBonus() + delta);
      }

      playersScores.put(key, playerScore);

      return Response.accepted().build();
   }

   @GET
   @Path("/{gameId}/{matchId}/{userId}/score")
   public Response getScore(@PathParam("gameId") String gameId,
                          @PathParam("matchId") String matchId,
                          @PathParam("userId") String userId) {
      Response response = scoringServiceError();
      if (response != null) {
         return response;
      }

      String key = getKey(gameId, matchId, userId);
      PlayerScore playerScore = playersScores.get(key);
      if (playerScore == null) {
         return Response.status(Response.Status.NOT_FOUND).build();
      }

      return Response.ok(playerScore, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Path("/{gameId}/ranking")
   public Response finalRanking(@PathParam("gameId") String gameId,
                                @QueryParam("max") Integer max) {
      Response response = scoringServiceError();
      if (response != null) {
         return response;
      }

      QueryFactory queryFactory = Search.getQueryFactory(playersScores);
      Query query = queryFactory.create(
            "from com.redhat.PlayerScore p WHERE p.human=true AND p.gameId='" + gameId
                  + "' ORDER BY p.score DESC").maxResults(max == null ? 1000 : max.intValue());

      List<PlayerScore> finalRanking = query.execute().list();
      return Response.ok(finalRanking, MediaType.APPLICATION_JSON_TYPE).build();
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

   private Response handleGameOver(String gameId, String matchId, String userId, Long timestamp, GameStatus status) {
      Response response = scoringServiceError();
      if (response != null) {
         return response;
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

   private Response scoringServiceError() {
      if(playersScores == null) {
         LOGGER.error("Unable to score, players-score cache does not exist.");
         return Response.ok("Unable to score, players-score cache does not exist.").build();
      }
      return null;
   }

   private String getKey(String gameId, String matchId, String userId) {
      return gameId + '-' + matchId + '-' + userId;
   }
}
