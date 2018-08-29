package it.unibo.osmos.redux.mvc.view.components.custom

import scalafx.beans.property.{DoubleProperty, StringProperty}
import scalafx.scene.control.TextFormatter.Change
import scalafx.scene.control.{TextField, TextFormatter}
import scalafx.util.converter.{DoubleStringConverter, NumberStringConverter}

class TitledDoubleField(override val title: StringProperty, private val value: DoubleProperty) extends TitledNode[TextField](title, vertical = false) {

  def this(title: String,value: DoubleProperty) {
    this(StringProperty(title), value)
  }

  /**
    * The maximum value
    */
  var maxValue: Double = Double.MaxValue
  /**
    * The minimum value
    */
  var minValue: Double = Double.MinValue

  /**
    * The node that will be shown after the text
    *
    * @return a node of type N <: Node
    */
  override def innerNode: TextField = new TextField(){
    text.delegate.bindBidirectional(value, new NumberStringConverter)
    editable = true
    prefWidth <== maxWidth
    textFormatter = new TextFormatter[Double](new DoubleStringConverter(), 0, { c: Change => {
      val input = c.getText
      val isNumber = input.matches("^[0-9]+(\\.[0-9]+)?$")
      if (!isNumber) c.setText("")
      if (isNumber && (maxValue < c.getControlNewText.toDouble || minValue > c.getControlNewText.toDouble)) c.setText("")
      c
    }})
  }
}
