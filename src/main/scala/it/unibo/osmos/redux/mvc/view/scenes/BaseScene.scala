package it.unibo.osmos.redux.mvc.view.scenes

import it.unibo.osmos.redux.mvc.controller.manager.files.StyleFileManager
import scalafx.scene.Scene
import scalafx.stage.Stage

/** BaseScene case class which holds the reference to the parent Stage instance */
case class BaseScene(parentStage: Stage) extends Scene {

  /** styles are reset each time a scene changes, so each time should be loaded */
  this.getStylesheets.addAll(StyleFileManager.getStyle)

}
