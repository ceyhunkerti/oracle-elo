package io.blue.actor.message

import io.blue.model.User

case class SendNewPasswordMail(user: User, password: String)