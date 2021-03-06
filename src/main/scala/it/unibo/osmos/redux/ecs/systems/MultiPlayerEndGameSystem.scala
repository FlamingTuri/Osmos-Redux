package it.unibo.osmos.redux.ecs.systems

import it.unibo.osmos.redux.ecs.entities.EntityType
import it.unibo.osmos.redux.ecs.entities.properties.composed.DeathProperty
import it.unibo.osmos.redux.ecs.systems.victoryconditions.AbsorbAllOtherPlayersCondition
import it.unibo.osmos.redux.multiplayer.server.Server
import it.unibo.osmos.redux.mvc.controller.levels.structure.VictoryRules
import it.unibo.osmos.redux.mvc.view.context.GameStateHolder
import it.unibo.osmos.redux.mvc.view.events.{GameLost, GameLostAsServer, GamePending, GameWon}

/** System managing the level's ending rules for multi-player matches
  *
  * @param levelContext object to notify the view of the end game result
  * @param victoryRules enumeration representing the level's victory rules
  */
case class MultiPlayerEndGameSystem(server: Server, levelContext: GameStateHolder, victoryRules: VictoryRules.Value) extends AbstractSystem[DeathProperty] {

  private val victoryCondition = victoryRules match {
    case VictoryRules.absorbAllOtherPlayers => AbsorbAllOtherPlayersCondition()
    case _ => throw new UnsupportedOperationException("Specified victory condition cannot be used in multi-player mode.")
  }

  override def update(): Unit = {
    if (isGameRunning) {
      val playerEntities = entities.filter(_.getTypeComponent.typeEntity == EntityType.Controlled).map(_.getUUID)
      val (alivePlayers, deadPlayers) = server.getLobbyPlayers.filter(_.isAlive) partition (p => playerEntities contains p.getUUID)
      val aliveCells = entities.filterNot(c => deadPlayers.map(_.getUUID) contains c.getUUID)

      //check
      for (
        (player, _, isServer) <- alivePlayers
          .map(i => (i, entities.find(_.getUUID == i.getUUID), i.getUUID == server.getUUID)) //get player, cell and isServer
          .filter(i => i._2.nonEmpty && victoryCondition.check(i._2.get, aliveCells)) //filter only players that have won
        if isGameRunning //when a winner is found do not check other players
      ) yield {
        //DEBUG ONLY: Logger.log(s"Victory detected for: ${player.getUsername} - ${player.getUUID} (server: $isServer)")("MultiPlayerEndGameSystem")
        if (isServer) {
          server.stopGame()
          levelContext.notify(GameWon)
        } else {
          server.stopGame(player.getUsername)
          levelContext.notify(GameLost)
        }
      }

      //notify all dead players and remove them
      if (isGameRunning) {
        deadPlayers.foreach(p => {
          //check if the server died
          val isServer = p.getUsername == server.getUsername
          //remove player from game and notify it (only if it's not the server itself)
          server.removePlayerFromGame(p.getUsername, !isServer)
          //if the server lost show the alternate UI
          if (isServer) levelContext.notify(GameLostAsServer)
        })
      }
    }
  }

  private def isGameRunning: Boolean = levelContext.gameCurrentState == GamePending || levelContext.gameCurrentState == GameLostAsServer
}

