package it.unibo.osmos.redux.mvc.view.components.menu

import it.unibo.osmos.redux.mvc.view.components.custom.StyledButton
import scalafx.geometry.Pos
import scalafx.scene.control.Button
import scalafx.scene.layout.VBox

/**
  * Center menu shown in MainMenu
  */
class MainMenuCenterBox(val listener: MainMenuCenterBoxListener) extends VBox(20.0) {

  alignment = Pos.Center

  /* Play button */
  val playButton = new StyledButton("Campaign Levels")
  /* Multiplayer button */
  val multiplayerButton = new StyledButton("Multiplayer")
  /* Editor button */
  val editorButton = new StyledButton("Editor Levels")
  /* Editor button */
  val settingsButton = new StyledButton("Settings")
  /* Exit button */
  val exitButton = new StyledButton("Exit")

  children = List(playButton, multiplayerButton, editorButton, settingsButton, exitButton)

  playButton.onAction = _ => listener.onPlayClick()
  multiplayerButton.onAction = _ => listener.onMultiPlayerClick()
  editorButton.onAction = _ => listener.onEditorClick()
  settingsButton.onAction = _ => listener.onSettingsClick()
  exitButton.onAction = _ => listener.onExitClick()
}

/**
  * Trait which gets notified when a MainMenuCenterBox event occurs
  */
trait MainMenuCenterBoxListener {

  /** Called when the user clicks on the back to menu button */
  def backToMainMenu()

  /**
    * Called when the user clicks on the play button
    */
  def onPlayClick()

  /**
    * Called when the user clicks on the multiplayer button
    */
  def onMultiPlayerClick()

  /**
    * Called when the user clicks on the editor button
    */
  def onEditorClick()

  /**
    * Called when the user clicks on the settings button
    */
  def onSettingsClick()

  /**
    * Called when the user clicks on the exit button
    */
  def onExitClick()
}