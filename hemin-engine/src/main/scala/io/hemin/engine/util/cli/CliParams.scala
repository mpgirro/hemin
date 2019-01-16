package io.hemin.engine.util.cli

import org.rogach.scallop.exceptions._
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}

class CliParams(args: List[String])
  extends ScallopConf(args) {

  version("test 1.2.3 (c) 2012 Mr Placeholder")
  banner("""Usage: test [OPTION]... [tree|palm] [OPTION]... [tree-name]
           |test is an awesome program, which does something funny
           |Options:
           |""".stripMargin)
  footer("\nFor all other tricks, consult the documentation!")
  shortSubcommandsHelp(true)
  val properties: Map[String, String] = props[String](descr = "some key-value pairs")
  val verbose: ScallopOption[Boolean] = opt[Boolean](descr = "use more verbose output")
  val amount: ScallopOption[Int] = opt[Int](
    name = "amount",
    descr = "how many objects do you need?")

  val help = new Subcommand("help")
  addSubcommand(help)


  val podcast = new Subcommand("podcast") {

    val check = new Subcommand("check") {
      val id: ScallopOption[String] = trailArg[String](
        name = "ID",
        descr = "Check Podcast by ID",
        required = true)
    }
    addSubcommand(check)

    val get = new Subcommand("get") {
      val id: ScallopOption[String] = trailArg[String](
        name = "ID",
        descr = "Get Podcast by ID",
        required = true)
    }
    addSubcommand(get)

    val episodes = new Subcommand("episodes") {
      val get = new Subcommand("get") {
        val id: ScallopOption[String] = trailArg[String](
          name = "ID",
          descr = "Get Episodes of Podcast by ID",
          required = true)
      }
      addSubcommand(get)
    }
    addSubcommand(episodes)

    val feeds = new Subcommand("feeds") {
      val get = new Subcommand("get") {
        val id: ScallopOption[String] = trailArg[String](
          name = "ID",
          descr = "Get Feeds of Podcast by ID",
          required = true)
      }
      addSubcommand(get)
    }
    addSubcommand(feeds)
  }
  addSubcommand(podcast)

  val feed = new Subcommand("feed") {
    val propose = new Subcommand("propose") {
      val url: ScallopOption[List[String]] = trailArg[List[String]](
        name = "URL [URL [...]]",
        descr = "Propose a new Feed by an URL",
        required = true)
    }
    addSubcommand(propose)
  }
  addSubcommand(feed)

  val tree = new Subcommand("tree") {
    val height: ScallopOption[Double] = opt[Double](
      name = "height",
      descr = "how tall should the tree be?")
    val name: ScallopOption[String] = trailArg[String](
      name = "tree name",
      descr = "tree name")
  }
  addSubcommand(tree)

  val palm = new Subcommand("palm") {
    val height: ScallopOption[Double] = opt[Double](
      name = "height",
      descr = "how tall should the palm be?"
    )
    val name: ScallopOption[String] = trailArg[String](
      name = "palm name",
      descr = "palm name",
      required = true,
    )
  }
  addSubcommand(palm)

  verify()

  override def onError(ex: Throwable): Unit = ex match {
    case Help("") => // main help was called
      builder.printHelp
    case Help(subcommandName) => // help for subcommand was called
      builder.findSubbuilder(subcommandName).get.printHelp
    case Version =>
      builder.vers.foreach(println)
    case Exit() => // catches both Help and Error
    case ScallopException(message) => // catches all excepitons
      println(message)
    case RequiredOptionNotFound(name) =>
      println("Required option missing: '%s'" format name)
      printHelp
    // you can also conveniently match on exceptions
    case other => throw other
  }

}
