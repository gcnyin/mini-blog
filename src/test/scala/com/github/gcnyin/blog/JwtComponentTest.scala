package com.github.gcnyin.blog

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pdi.jwt.exceptions.JwtExpirationException

import java.time.Instant

class JwtComponentTest extends AnyFlatSpec with Matchers {
  it should "createToken" in {
    val jwtComponent = new JwtComponent("123", Instant.ofEpochMilli(0))
    val except = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
      "eyJzdWIiOiJUb20iLCJleHAiOjYwNDgwMCwiaWF0IjowfQ." +
      "oGuVPwL1rJHi9T1eFddtWLCiHFnlB-F80VutEhbt0BU"

    val token = jwtComponent.createToken("Tom")

    token should be(except)
  }

  it should "parseToken successfully" in {
    val username = "Tom"
    val jwtComponent = new JwtComponent("123", Instant.now)
    val token = jwtComponent.createToken(username)

    val e = jwtComponent.parseToken(token)

    e should be(Right(username))
  }

  it should "failed to parseToken" in {
    val username = "1"
    val jwtComponent = new JwtComponent("123", Instant.ofEpochMilli(0))
    val token = jwtComponent.createToken(username)

    val e = jwtComponent.parseToken(token)

    assert(e.isLeft)
  }
}
