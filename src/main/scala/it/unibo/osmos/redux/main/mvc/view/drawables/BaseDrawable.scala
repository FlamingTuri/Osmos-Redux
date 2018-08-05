package it.unibo.osmos.redux.main.mvc.view.drawables

import scalafx.scene.canvas.GraphicsContext

/**
  * Abstract base Drawable class which holds spacial coordinates and the reference of the GraphicContext on which it will be written
  * @param graphicsContext the GraphicContext on which the Drawable will be written on
  */
abstract class BaseDrawable(val graphicsContext: GraphicsContext) extends Drawable {

}
