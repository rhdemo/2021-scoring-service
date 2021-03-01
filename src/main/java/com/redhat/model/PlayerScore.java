package com.redhat.model;

import org.infinispan.protostream.annotations.ProtoDoc;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.util.Objects;

@ProtoDoc("@Indexed")
public class PlayerScore implements Comparable {

   public static final String PLAYERS_SCORES = "players-scores";
   private String userId;
   private String matchId;
   private String gameId;
   private Boolean human;
   private Integer score;
   private Integer lastIncrement;
   private Long timestamp;
   private GameStatus gameStatus;

   @ProtoFactory
   public PlayerScore(String userId, String matchId, String gameId, Boolean human, Integer score, Long timestamp,
                      GameStatus gameStatus) {
      this.userId = userId;
      this.matchId = matchId;
      this.gameId = gameId;
      this.human = human;
      this.score = score;
      this.timestamp = timestamp;
      this.gameStatus = gameStatus;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;
      PlayerScore that = (PlayerScore) o;
      return Objects.equals(userId, that.userId) && Objects
            .equals(matchId, that.matchId) && Objects.equals(gameId, that.gameId);
   }

   @Override
   public int compareTo(Object o) {
      PlayerScore cp = (PlayerScore)o;
      if(this.getScore() == cp.getScore()) {
         if (this.getTimestamp() == 0 || cp.getTimestamp() == 0) {
            return 0;
         }
         return this.getTimestamp().compareTo(cp.getTimestamp());
      }
      return this.getScore().compareTo(cp.getScore());
   }

   @Override
   public int hashCode() {
      return Objects.hash(userId, matchId, gameId);
   }

   @ProtoField(number = 1)
   public String getUserId() {
      return userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   @ProtoField(number = 2)
   public String getMatchId() {
      return matchId;
   }

   public void setMatchId(String matchId) {
      this.matchId = matchId;
   }

   @ProtoField(number = 3)
   public String getGameId() {
      return gameId;
   }

   public void setGameId(String gameId) {
      this.gameId = gameId;
   }

   @ProtoField(number = 4)
   public Boolean isHuman() {
      return human;
   }

   public void setHuman(Boolean human) {
      this.human = human;
   }

   @ProtoField(number = 5)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.NO)")
   public Integer getScore() {
      return score;
   }

   public void setScore(Integer score) {
      this.score = score;
   }

   @ProtoField(number = 6)
   @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.NO)")
   public Long getTimestamp() {
      return timestamp;
   }

   public void setTimestamp(Long timestamp) {
      this.timestamp = timestamp;
   }

   @ProtoField(number = 7)
   public GameStatus getGameStatus() {
      return gameStatus;
   }

   public void setGameStatus(GameStatus gameStatus) {
      this.gameStatus = gameStatus;
   }
}