package hemin.engine.util

import org.hashids.Hashids

class IdGenerator (shardId: Int) {

  private val ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  private val hashids = new Hashids("Bit useless for my purpose, but why not", 0, ALPHABET)
  private var seq = 0

  def newId: String = this.synchronized {
    val now = System.currentTimeMillis
    val id = hashids.encode(seq, shardId, now)
    seq = (seq + 1) % 1024
    id
  }

}
