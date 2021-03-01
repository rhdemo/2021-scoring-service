package com.redhat;

import com.redhat.model.PlayerScore;
import com.redhat.model.Shot;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class InfinispanInit {

   public static final String INDEXED_PROTOBUF = "indexed-protobuf";
   @Inject
   RemoteCacheManager cacheManager;

   @ConfigProperty(name = "configureInfinispan")
   Boolean configureInfinispan;

   void onStart(@Observes StartupEvent ev) {
      if(configureInfinispan) {
         try{
            cacheManager.administration()
                  .createTemplate(INDEXED_PROTOBUF, new XMLStringConfiguration(TEST_CACHE_XML_CONFIG));
         } catch(Exception ex) {
            // Do nothing
         }

         cacheManager.administration().getOrCreateCache(PlayerScore.PLAYERS_SCORES, INDEXED_PROTOBUF);
         cacheManager.administration().getOrCreateCache(Shot.PLAYERS_SHOTS, INDEXED_PROTOBUF);
      }
   }

   private static final String TEST_CACHE_XML_CONFIG =
               "<infinispan><cache-container>" +
               "  <distributed-cache-configuration name=\"indexed-protobuf\" statistics=\"true\">" +
               "    <memory storage=\"HEAP\"/>" +
               "    <encoding>" +
               "        <key media-type=\"application/x-protostream\"/>" +
               "        <value media-type=\"application/x-protostream\"/>" +
               "    </encoding>" +
               "    <indexing enabled=\"true\">" +
               "        <indexed-entities>" +
               "           <indexed-entity> com.redhat.PlayerScore </indexed-entity>" +
               "        </indexed-entities>" +
               "    </indexing>" +
               "  </distributed-cache-configuration>" +
               "</cache-container></infinispan>";
}
