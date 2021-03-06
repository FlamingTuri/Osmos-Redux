package it.unibo.osmos.redux

import it.unibo.osmos.redux.ecs.entities._
import it.unibo.osmos.redux.ecs.entities.properties.composed.DeathProperty
import it.unibo.osmos.redux.ecs.systems.AbstractSystem
import org.scalatest.FunSuite

/**
  * Spy class to capture entities number
  */
case class SystemSpy() extends AbstractSystem[DeathProperty] {
  override def update(): Unit = ???

  def entitiesNumber: Int = entities.size
}

class TestEntityManager extends FunSuite {
  val ce: CellEntity = CellBuilder().buildCellEntity()
  val pce: PlayerCellEntity = CellBuilder().buildPlayerEntity()

  test("Add and remove entity") {
    val systemSpy = SystemSpy()

    //Add cell entities
    EntityManager.add(pce)
    assert(systemSpy.entitiesNumber == 1)
    EntityManager.add(ce)
    assert(systemSpy.entitiesNumber == 2)

    //Remove cell entities
    EntityManager.delete(pce)
    assert(systemSpy.entitiesNumber == 1)
    EntityManager.delete(ce)
    assert(systemSpy.entitiesNumber == 0)

    //Clear entity manager
    EntityManager.clear()
    EntityManager.add(ce)
    assert(systemSpy.entitiesNumber == 0)
    EntityManager.clear()
  }
}
