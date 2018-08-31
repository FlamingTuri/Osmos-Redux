package it.unibo.osmos.redux.mvc.controller

import it.unibo.osmos.redux.ecs.engine.GameEngine
import it.unibo.osmos.redux.ecs.entities.PlayerCellEntity
import it.unibo.osmos.redux.multiplayer.client.Client
import it.unibo.osmos.redux.multiplayer.common.{ActorSystemHolder, MultiPlayerMode}
import it.unibo.osmos.redux.multiplayer.server.Server
import it.unibo.osmos.redux.mvc.model.SinglePlayerLevels.LevelInfo
import it.unibo.osmos.redux.mvc.model.{Level, MultiPlayerLevels, SinglePlayerLevels, SoundsType}
import it.unibo.osmos.redux.mvc.view.components.multiplayer.User
import it.unibo.osmos.redux.mvc.view.context._
import it.unibo.osmos.redux.mvc.view.events.{AbortLobby, _}
import it.unibo.osmos.redux.utils.{Constants, Logger}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import scala.util.{Failure, Success}

/**
  * Controller base trait
  */
trait Controller {
  type MultiPlayerMode = MultiPlayerMode.Value
  type LevelContextType = LevelContextType.Value

  /**
    * Initializes the level and the game engine.
    * @param levelContext The level context.
    * @param chosenLevel The name of the chosen level.
    */
  def initLevel(levelContext: LevelContext, chosenLevel: String): Unit

  /**
    * Initializes the multi-player lobby and the server or client.
    * @param user The user config
    * @param lobbyContext The lobby context
    * @return Promise that completes with true if the lobby is initialized successfully; otherwise false.
    */
  def initLobby(user: User, lobbyContext: LobbyContext): Promise[Boolean]

  /**
    * Initializes the multi-player level and the game engine.
    * @return Promise that completes with true if the level is initialized successfully; otherwise false.
    */
  def initMultiPlayerLevel(): Promise[Boolean]

  /**
    * Starts the level.
    */
  def startLevel(): Unit

  /**
    * Stops the level.
    */
  def stopLevel(): Unit

  /**
    * Pauses the level.
    */
  def pauseLevel(): Unit

  /**
    * Resumes the level.
    */
  def resumeLevel(): Unit

  /**
    * Saves a new custom level.
    * @param customLevel The custom level.
    * @return True, if the operation is successful; otherwise false.
    */
  def saveNewCustomLevel(customLevel:Level): Boolean

  /**
    * Gets all the levels in the campaign.
    * @return The list of LevelInfo.
    */
  def getSinglePlayerLevels:List[LevelInfo] = SinglePlayerLevels.getLevels

  /**
    * Gets all multi-player levels.
    * @return The list of multi-player levels.
    */
  def getMultiPlayerLevels: List[String] = MultiPlayerLevels.getLevels

  /**
    * Gets all custom levels filename.
    * @return The list of custom levels filename.
    */
  def getCustomLevels: List[LevelInfo]

  /**
    * Get requested sound path
    * @param soundType SoundsType.Value
    * @return Some(String) if path exists
    */
  def getSoundPath(soundType: SoundsType.Value): Option[String]

}

case class ControllerImpl() extends Controller with GameStateHolder {

  implicit val who: String = "Controller"

  var lastLoadedLevel:Option[String] = None
  var engine:Option[GameEngine] = None

  //multi-player variables
  private var multiPlayerMode: Option[MultiPlayerMode] = None
  private var server: Option[Server] = None
  private var client: Option[Client] = None

  override def initLevel(levelContext: LevelContext, chosenLevel: String): Unit = {
    Logger.log("initLevel")

    var loadedLevel:Option[Level] = None
    //TODO custom level logic
    //if (isCustomLevel) {
    // loadedLevel = FileManager.loadCustomLevel(chosenLevel)
    // lastLoadedLevel = None
    //}
    //else {
    loadedLevel = FileManager.loadResource(chosenLevel)
    lastLoadedLevel = Some(chosenLevel)
    //}

    if(loadedLevel.isDefined) {

      loadedLevel.get.isSimulation = levelContext.levelContextType == LevelContextType.simulation

      val player = loadedLevel.get.entities.find(_.isInstanceOf[PlayerCellEntity])
      if (player.isEmpty && !loadedLevel.get.isSimulation) throw new IllegalStateException("")
      //assign current player uuid to the
      levelContext.setPlayerUUID(player.get.getUUID)

      //create and initialize the game engine
      if(engine.isEmpty) engine = Some(GameEngine())
      //TODO: engine must need a GameStateHolder for the EndGameSystem
      engine.get.init(loadedLevel.get, levelContext/*, this*/)
      levelContext.setupLevel(loadedLevel.get.levelMap.mapShape)
    } else {
      println("Level ", chosenLevel, " not found! is a custom level? "/*, isCustomLevel*/)
    }
  }

  override def initLobby(user: User, lobbyContext: LobbyContext): Promise[Boolean] = {
    Logger.log("initLobby")

    val promise = Promise[Boolean]()

    //subscribe to lobby context to intercept exit from lobby click
    lobbyContext.subscribe {
      //if user is defined that the event is from the user and not from the server
      case LobbyEventWrapper(AbortLobby, Some(_)) =>
        multiPlayerMode match {
          case Some(MultiPlayerMode.Server) =>
            if (server.nonEmpty) {
              server.get.closeLobby()
              server.get.kill()
            }
            client = None
          case Some(MultiPlayerMode.Client) =>
            if (client.nonEmpty) {
              client.get.leaveLobby()
              client.get.kill()
            }
            server = None
          case _ => //do not
        }
      case _ => //do not
    }

    multiPlayerMode = Some(if (user.isServer) MultiPlayerMode.Server else MultiPlayerMode.Client)
    multiPlayerMode match {
      case Some(MultiPlayerMode.Server) =>

        //initialize the server and creates the lobby
        val server = Server(user.username)
        server.bind(ActorSystemHolder.createActor(server))
        //create lobby
        server.createLobby(lobbyContext)
        //save server reference
        this.server = Some(server)
        promise.success(true)

      case Some(MultiPlayerMode.Client) =>

        //initialize the client, connects to the server and enters the lobby
        val client = Client()
        client.bind(ActorSystemHolder.createActor(client))
        client.connect(user.ip, user.port).future onComplete {
          case Success(true) => client.enterLobby(user.username, lobbyContext).future onComplete {
            case Success(true) =>
              this.client = Some(client)
              //creates the level context
              //TODO: think about a better way, technically getUUID method of the client won't be called until the game is started (the GameStarted message will carry along this value).
              val levelContext = LevelContext(Constants.defaultClientUUID)
              //initializes the game
              client.initGame(levelContext)
              //fulfill promise
              promise success true
            case Success(false) => promise success false
            case Failure(t) => promise failure t
          }
          case Success(false) => promise success false
          case Failure(t) => promise failure t
        }
      case _ =>
        promise failure new IllegalArgumentException("Cannot initialize the lobby if the multi-player mode is not defined")
    }
    promise
  }

  override def initMultiPlayerLevel(): Promise[Boolean] = {
    Logger.log("initMultiPlayerLevel")

    val promise = Promise[Boolean]()

    //load level definition
    //TODO: for not there is only one multi-player level (lobbyContext has the chosenLevel property)
    val loadedLevel = FileManager.loadResource("1", isMultiPlayer = true).get

    multiPlayerMode.get match {
      case MultiPlayerMode.Server =>
        //assign clients to players and wait confirmation
        server.get.initGame(loadedLevel).future onComplete {
          case Success(_) =>
            //create the engine
            if (engine.isEmpty) engine = Some(GameEngine())
            //initialize the engine and let him create the levelContext
            val levelContext = engine.get.init(loadedLevel, server.get)

            //signal server that the game is ready to be started
            server.get.startGame(levelContext)
            //tell view to actually start the game
            levelContext.setupLevel(loadedLevel.levelMap.mapShape)

            //fulfill the promise
            promise success true
          case Failure(t) => promise failure t
        }
      case _ =>
        promise failure new IllegalStateException("Unable to initialize multi-player level if no lobby have been created.")
    }
    promise
  }

  override def startLevel(): Unit = {
    Logger.log("startLevel")

    multiPlayerMode match {
      case Some(MultiPlayerMode.Server) | None => if (engine.isDefined) engine.get.start()
      case _ =>
    }
  }

  override def stopLevel(): Unit = {
    Logger.log("stopLevel")

    multiPlayerMode match {
      case Some(MultiPlayerMode.Client) =>
        if (client.isDefined) client.get.leaveGame()
      case _ =>
        if (server.isDefined) server.get.stopGame()
        if (engine.isDefined) engine.get.stop()
    }
  }

  override def pauseLevel(): Unit = {
    Logger.log("pauseLevel")

    multiPlayerMode match {
      case None => if (engine.isDefined) engine.get.pause()
      case _ => throw new UnsupportedOperationException("A multi-player level cannot be paused.")
    }
  }

  override def resumeLevel(): Unit = {
    Logger.log("resumeLevel")

    multiPlayerMode match {
      case None => if (engine.isDefined) engine.get.resume()
      case _ => throw new UnsupportedOperationException("A multi-player level cannot be resumed.")
    }
  }

  override def saveNewCustomLevel(customLevel: Level): Boolean =
    FileManager.saveLevel(customLevel, customLevel.levelId).isDefined

   /**
    * A generic definition of the game state
    *
    * @return a GameStateEventWrapper
    *///TODO useless for controller
  override def gameCurrentState: GameStateEventWrapper = ???

  override def getSoundPath(soundType: SoundsType.Value): Option[String] = soundType match {
    case SoundsType.menu => Some(FileManager.loadMenuMusic())
    case SoundsType.level => Some(FileManager.loadLevelMusic())
    case SoundsType.button => Some(FileManager.loadButtonsSound())
    case _ => println("Sound type not managed!! [Controller getSoundPath]")
              None
  }

  /**
    * Called on a event T type
    *
    * @param event the event
    */
  override def notify(event: GameStateEventWrapper): Unit = {
    if(lastLoadedLevel.isDefined) {
      SinglePlayerLevels.newEndGameEvent(event, lastLoadedLevel.get)
      FileManager.saveUserProgress(SinglePlayerLevels.userStatistics())
    }
  }

  /**
    * Gets all custom levels filename.
    *
    * @return The list of custom levels filename.
    */
  override def getCustomLevels: List[LevelInfo] = FileManager.customLevelsFilesName match {
    case Success(customLevels) => customLevels
    case Failure(exception) => Logger.log(exception.getMessage)
                               List()
  }
}
