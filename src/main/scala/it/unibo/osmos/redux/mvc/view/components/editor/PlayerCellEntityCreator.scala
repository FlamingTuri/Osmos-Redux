package it.unibo.osmos.redux.mvc.view.components.editor

import it.unibo.osmos.redux.ecs.entities.{CellEntity, PlayerCellEntity}
import it.unibo.osmos.redux.ecs.entities.builders.{CellBuilder, PlayerCellBuilder}

/**
  * A panel showing input nodes which is also capable of providing the requested PlayerCellEntity
  */
class PlayerCellEntityCreator extends AbstractSpawnerCellEntityCreator {

  override def configureBuilder(builder: CellBuilder, withEntityType: Boolean = true): Unit = {
    builder match {
      case pce: PlayerCellBuilder =>
        super.configureBuilder(pce, withEntityType = true)
        pce.withSpawner(canSpawn.value)
      case _ => throw new IllegalArgumentException("PlayerCellEntityCreator must use a PlayerCellBuilder")
    }
  }

  override def create(): CellEntity = {
    val builder = PlayerCellBuilder()
    configureBuilder(builder)
    builder.build
  }

}
