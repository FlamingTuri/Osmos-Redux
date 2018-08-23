package it.unibo.osmos.redux.mvc.view.scenes

import it.unibo.osmos.redux.mvc.view.components.multiplayer.{User, UserWithProperties}
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.TableColumn._
import scalafx.scene.control.{Button, TableColumn, TableView}
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.stage.Stage

/**
  * Lobby showing other clients or servers playing in multiplayer
  * @param parentStage the parent stage
  * @param listener the MultiPlayerLobbySceneListener
  * @param user the user who requested to enter the lobby
  */
class MultiPlayerLobbyScene(override val parentStage: Stage, val listener: MultiPlayerLobbySceneListener, val user: User) extends BaseScene(parentStage) {

  private val userList = ObservableBuffer[UserWithProperties](
    User("Marco", "0.0.0.0", "0000", isServer = true).getUserWithProperty,
    User("Davide", "0.0.0.1", "0001", isServer = false).getUserWithProperty,
    User("Placu", "0.0.0.2", "0002", isServer = false).getUserWithProperty,
    User("Turi", "0.0.0.3", "0003", isServer = false).getUserWithProperty,
    User("Proc", "0.0.0.4", "0004", isServer = false).getUserWithProperty
  )

  val usersTable: TableView[UserWithProperties] = new TableView[UserWithProperties](userList) {
    columns ++= List(
      new TableColumn[UserWithProperties, String]() {
        text = "Username"
        cellValueFactory = {_.value.username}
      }, new TableColumn[UserWithProperties, String]() {
        text = "IP"
        cellValueFactory = {_.value.ip}
      }, new TableColumn[UserWithProperties, String]() {
        text = "Port"
        cellValueFactory = {_.value.port}
      }
    )
  }

  private val container: VBox = new VBox(5.0) {

    maxWidth <== parentStage.width / 4
    maxHeight <== parentStage.height / 4

    alignment = Pos.Center
    children = Seq(usersTable)

  }

  private val goBack = new Button("Go back") {
    onAction = _ => {}
  }
  private val goToLobby = new Button("Go to lobby") {
    visible = user.isServer
    onAction = _ => {}
  }

  /* Requesting a structured layout */
  private val rootLayout: BorderPane = new BorderPane {
    padding = Insets(130)
    alignmentInParent = Pos.Center
    /* Setting the upper MenuBar */
    center = container
    bottom = new HBox(30.0, goBack, goToLobby) {
      alignment = Pos.Center
    }
  }

  /* Enabling the layout */
  root = rootLayout

}

/**
  * Trait used by MultiPlayerLobbyScene to notify events which need to be managed by the View
  */
trait MultiPlayerLobbySceneListener {

}
