package io.blue.model

import java.util.Date
import javax.persistence._
import org.hibernate.validator.constraints._
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import javax.validation.constraints.{NotNull}
import scala.beans.BeanProperty
import com.fasterxml.jackson.annotation._
import com.fasterxml.jackson.databind.annotation.JsonSerialize

import Role._

@Entity(name="users")
class User {

  def this(id: Long) {
    this()
    this.id = id
  }

  @Id
  @GeneratedValue
  @BeanProperty
  var id: Long = _

  @BeanProperty
  @NotNull(message="error.username.notNull")
  @Column(unique = true)
  var username: String = _

  @BeanProperty
  @JsonIgnore
  var password: String = _

  @BeanProperty
  @NotNull(message="error.role.notNull")
  var role: String = Role.GUEST

  @BeanProperty
  @NotNull(message="error.name.notNull")
  var name: String = _

  @BeanProperty
  @Column(unique = true)
  @NotNull(message = "error.email.notNull")
  @Email(message="{error.email.valid}")
  var email: String = _

  @BeanProperty
  @NotNull
  var system: Boolean = false

  @BeanProperty
  @NotNull
  var locked: Boolean = false


  @BeanProperty
  @Column(columnDefinition = "varchar(max)")
  var options: String = _


  @PrePersist @PreUpdate private def prepare = {
    username = if (username == null)  null else username.toLowerCase
    email = if (email == null)  null else email.toLowerCase
  }

  @JsonIgnore
  def isMaster = role == Role.MASTER

}
