package it.unibo.osmos.redux.ecs.entities

import java.util.UUID

import it.unibo.osmos.redux.ecs.components._

trait SentientCellEntity extends CellEntity with SentientProperty {

}

object SentientCellEntity {
  def apply(acceleration: AccelerationComponent,
            collidable: CollidableComponent,
            dimension: DimensionComponent,
            position: PositionComponent,
            speed: SpeedComponent,
            visible: VisibleComponent): SentientCellEntity = SentientCellEntityImpl(CellEntity(acceleration,
    collidable, dimension, position, speed, visible, TypeComponent(EntityType.Sentient)))

  private case class SentientCellEntityImpl(cellEntity: CellEntity) extends SentientCellEntity {

    override def getUUID: UUID = cellEntity.getUUID

    override def getAccelerationComponent: AccelerationComponent = cellEntity.getAccelerationComponent

    override def getSpeedComponent: SpeedComponent = cellEntity.getSpeedComponent

    override def getCollidableComponent: CollidableComponent = cellEntity.getCollidableComponent

    override def getTypeComponent: TypeComponent = cellEntity.getTypeComponent

    override def getPositionComponent: PositionComponent = cellEntity.getPositionComponent

    override def getVisibleComponent: VisibleComponent = cellEntity.getVisibleComponent

    override def getDimensionComponent: DimensionComponent = cellEntity.getDimensionComponent
  }

}
