package controllers

import play.api.mvc.{Result, Action, Session, Request, RequestHeader, AnyContent, Results, BodyParser, BodyParsers}

import com.feth.play.module.pa.PlayAuthenticate.{getProvider}
import com.feth.play.module.pa.user.AuthUser

import models.User

trait Authentication {
  val ORIGINAL_URL = "pa.url.orig"
  val USER_KEY = "pa.u.id"
  val PROVIDER_KEY = "pa.p.id"
  val EXPIRES_KEY = "pa.u.exp"
  val SESSION_ID_KEY = "pa.s.id"
  
  val NO_EXPIRATION = -1L

  def getUser(implicit session: Session): Option[AuthUser] = 
    for { 
      provider <- session.get(PROVIDER_KEY)
      id <- session.get(USER_KEY)
    } yield getProvider(provider).getSessionAuthUser(id, getExpiration)
  
  def getExpiration(implicit session: Session): Long = 
    try {
      session.get(EXPIRES_KEY).map(_.toLong).getOrElse(NO_EXPIRATION)
    } catch {
      case _: NumberFormatException => NO_EXPIRATION
    }
  
  implicit def currentUser(implicit request: RequestHeader): Option[User] =
    getUser(request.session).map(User.findByAuthUserIdentity).filter(_ != null)
  
  def Authenticated[A](p: BodyParser[A])(f: User => Request[A] => Result) = {
    Action(p) { implicit request: Request[A] =>
      currentUser.map { user: User =>
        f(user)(request)
      }.getOrElse(Results.Unauthorized)
    }
  }
  
  def Authenticated(f: User => Request[AnyContent] => Result): Action[AnyContent] = {
    Authenticated(BodyParsers.parse.anyContent)(f)
  }
  
}
