package com.sksamuel.scapegoat.inspections

import com.sksamuel.scapegoat.{Inspection, InspectionContext, Inspector, Levels}

import scala.collection.mutable

/** @author Stephen Samuel */
class LonelySealedTrait extends Inspection {

  override def inspector(context: InspectionContext): Inspector = new Inspector(context) {

    import context.global._

    private val sealedClasses = mutable.HashMap[String, ClassDef]()
    private val implementedClasses = mutable.HashSet[String]()

    override def postInspection(): Unit = {
      for ( (name, cdef) <- sealedClasses ) {
        if (!implementedClasses.contains(name)) {
          context.warn("Lonely sealed trait",
            cdef.pos,
            Levels.Error,
            s"Sealed trait ${cdef.name} has no implementing classes",
            LonelySealedTrait.this)
        }
      }
    }

    override def traverser = new context.Traverser {

      override def inspect(tree: Tree): Unit = {
        tree match {
          case cdef@ClassDef(mods, name, _, _) if mods.isSealed => sealedClasses.put(cdef.name.toString, cdef)
          case ClassDef(_, name, _, Template(parents, _, _)) =>
            parents.foreach(parent => implementedClasses.add(parent.toString()))
          case ModuleDef(_, name, Template(parents, _, _)) =>
            parents.foreach(parent => implementedClasses.add(parent.toString()))
          case _ => continue(tree)
        }
      }
    }
  }
}
