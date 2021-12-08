package com.github.gcnyin.blog

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class ServiceLogicTest extends AnyFlatSpec with Matchers {
  "BCryptPasswordEncoder" should "encode and decode" in {
    val encoder = new BCryptPasswordEncoder()
    val rawPassword = "123456"
    val password = encoder.encode(rawPassword)
    encoder.matches(rawPassword, password) should be(true)
  }
}
