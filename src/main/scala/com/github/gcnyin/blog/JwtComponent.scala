package com.github.gcnyin.blog

import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.{Duration, Instant}

class JwtComponent(key: String, currentTime: => Instant) {
  private val algo: JwtAlgorithm = JwtAlgorithm.HS256

  def createToken(username: String): String = {
    val time = currentTime
    val claim = JwtClaim(
      expiration = Some(time.plus(Duration.ofDays(7)).getEpochSecond),
      issuedAt = Some(time.getEpochSecond),
      subject = Some(username)
    )
    JwtCirce.encode(claim, key, algo)
  }

  def parseToken(token: String): Either[Model.Message, String] =
    JwtCirce
      .decodeAll(token, key, Seq(JwtAlgorithm.HS256))
      .toOption
      .flatMap(_._2.subject).toRight(Model.Message("invalid token"))
}
