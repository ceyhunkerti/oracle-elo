package io.blue.actor.message

import io.blue.model.User

case class SendUserPasswordMail(user: User, password: String)