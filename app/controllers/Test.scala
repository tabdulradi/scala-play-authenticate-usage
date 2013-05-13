package controllers
import play.api.mvc.{Result, Action, Controller, WebSocket}
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.{Play, Logger}
import play.api.libs.iteratee._
import Play.current
import scala.Some

import models.User;

import com.feth.play.module.pa.user.AuthUser


object Test extends Controller with Authentication {

  def helloPublic = Action { implicit request =>
    Ok("Hello, " + currentUser.map(_.name).getOrElse("Guest"))
  }
  
  def helloWithAuthentication = Authenticated { implicit user: User => implicit request =>
    Ok("Hi, " + user.name)
  }
  
  import play.api.mvc.BodyParsers.parse.tolerantText
  def helloWithBodyParser = Authenticated(tolerantText) { implicit user => implicit request =>
    Ok("Hi, " + user.name)
  }
  
  def helloWebSockets = WebSocket.using[String] { implicit request => 
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }
    
    val out = Enumerator(currentUser.map(_.name).getOrElse("Guest"))
    
    (in, out)
  }

}
