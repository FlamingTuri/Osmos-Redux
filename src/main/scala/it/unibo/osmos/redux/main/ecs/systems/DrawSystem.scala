package it.unibo.osmos.redux.main.ecs.systems

import it.unibo.osmos.redux.main.ecs.entities.EMEvents.{EntityCreated, EntityDeleted}
import it.unibo.osmos.redux.main.ecs.entities._
import it.unibo.osmos.redux.main.mvc.view.levels.LevelContext
import it.unibo.osmos.redux.main.mvc.view.drawables.DrawableWrapper

/**
  * System to draw all the entity
  * @param levelContext levelContext for communicate the entities to the view
  * @param priority system priority
  */
case class DrawSystem(levelContext: LevelContext, override val priority: Int) extends System[DrawableProperty](priority) {

  private var player: Option[DrawableProperty] = None

  override def notify(event: EMEvents.EntityManagerEvent): Unit = {
    if (event.entity.isInstanceOf[PlayerCellEntity]) {
      event match {
        case event: EntityCreated => player = Some(event.entity.asInstanceOf[DrawableProperty])
        case _: EntityDeleted => player = None
      }
    } else {
      super.notify(event)
    }
  }

  override def getGroupProperty(): Class[_ <: Property] = classOf[DrawableProperty]

  override def update(): Unit = levelContext.drawEntities(getPlayerEntity, getEntities)

  private def getPlayerEntity: Option[DrawableWrapper] =
    player filter(p => p.getVisibleComponent.isVisible()) map(p => drawablePropertyToDrawableWrapper(p))

  private def getEntities: List[DrawableWrapper] =
    entities filter(e => e.getVisibleComponent.isVisible()) map(e => drawablePropertyToDrawableWrapper(e)) toList

  private def drawablePropertyToDrawableWrapper(entity: DrawableProperty): DrawableWrapper =
    DrawableWrapper(entity.getPositionComponent.point,
                    entity.getDimensionComponent.radius,
                    entity.getTypeComponent.typeEntity)
}