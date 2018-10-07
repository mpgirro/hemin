package io.disposia.engine.domain

import java.time.LocalDateTime

case class Image (
  id: Option[String]               = None,
  associateId: Option[String]      = None,
  data: Option[Array[Byte]]        = None,
  hash: Option[String]             = None,
  name: Option[String]             = None,
  contentType: Option[String]      = None,
  size: Option[Long]               = None,
  createdAt: Option[LocalDateTime] = None,
)
