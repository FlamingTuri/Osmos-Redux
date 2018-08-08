package it.unibo.osmos.redux.main.ecs.components

/**
  * Component of the speed vector
  */
trait Speed {

  /**
    * Getter. Return the speed of the x coordinate
    * @return the speed
    */
  def speedX: Int

  /**
    * Getter. Return the speed of the y coordinate
    * @return the speed
    */
  def speedY: Int

  /**
    * Setter. Set the new speed of the x coordinate
    * @param speed the new speed
    */
  def speedX_(speed: Int): Unit

  /**
    * Setter. Set the new speed of the y coordinate
    * @param speed the new speed
    */
  def speedY_(speed: Int): Unit
}

object Speed {
  def apply(speedX: Int, speedY: Int): Speed = new SpeedImpl(speedX,speedY)

  private case class SpeedImpl(var _speedX: Int, var _speedY: Int) extends Speed {
    override def speedX: Int = _speedX

    override def speedY: Int = _speedY

    override def speedX_(speed: Int): Unit = _speedX = speed

    override def speedY_(speed: Int): Unit = _speedY = speed
  }
}