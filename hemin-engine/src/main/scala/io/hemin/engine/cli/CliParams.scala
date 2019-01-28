package io.hemin.engine.cli

import io.hemin.engine.HeminEngine
import org.rogach.scallop.exceptions._
import org.rogach.scallop.{ScallopConf, ScallopOption, Subcommand}

class CliParams(args: List[String])
  extends ScallopConf(args) {

  version(s"Hemin Engine CLI v${HeminEngine.version} (c) 2018 Maximilian Irro")
  banner("""Usage: [OPTION]... [tree|palm] [OPTION]... [tree-name]
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

  val help = new Subcommand("help") {
    descr("Show help information")
  }
  addSubcommand(help)

  trait ID { _: ScallopConf => // <<< NB: otherwise we will get Option identifier 'trailArg' is not unique (calls `opt` on the parent class `Conf` twice)
    val id: ScallopOption[String] = trailArg[String](
      name = "ID",
      required = true)
  }

  val search = new Subcommand("search") {
    descr("Run search")
    shortSubcommandsHelp(true)
    val query: ScallopOption[List[String]] = trailArg[List[String]](
      name = "QUERY [QUERY [...]]",
      required = true)
    val pageNumber: ScallopOption[Int] = opt[Int](
      name = "pageNumber",
      short = 'p',
      argName = "NUM",
      descr = "Page number of search result")
    val pageSize: ScallopOption[Int] = opt[Int](
      name = "pageSize",
      short = 's',
      argName = "NUM",
      descr = "Page size of search result")
  }
  addSubcommand(search)

  val podcast = new Subcommand("podcast") {
    descr("Operations on Podcasts")
    shortSubcommandsHelp(true)
    val check = new Subcommand("check") with ID {
      descr("Check Podcast by ID")
    }
    addSubcommand(check)

    val get = new Subcommand("get") with ID {
      descr("Get Podcast by ID")
    }
    addSubcommand(get)

    val episodes = new Subcommand("episodes") {
      descr("Operations on a Podcast's Episodes")
      shortSubcommandsHelp(true)
      val get = new Subcommand("get") with ID {
        descr("Get Episodes of Podcast by ID")
      }
      addSubcommand(get)
    }
    addSubcommand(episodes)

    val feeds = new Subcommand("feeds") {
      descr("Operations on a Podcast's Feeds")
      shortSubcommandsHelp(true)
      val get = new Subcommand("get") with ID {
        descr("Get Feeds of Podcast by ID")
      }
      addSubcommand(get)
    }
    addSubcommand(feeds)
  }
  addSubcommand(podcast)

  val episode = new Subcommand("episode") {
    descr("Operations on Episodes")
    shortSubcommandsHelp(true)
    val get = new Subcommand("get") with ID {
      descr("Get Episode by ID")
    }
    addSubcommand(get)

    val chapters = new Subcommand("chapters") {
      descr("Operations on anEpisode's Chapters")
      shortSubcommandsHelp(true)
      requireSubcommand()
      val get = new Subcommand("get") with ID {
        descr("Get Chapters of Episode by ID")
      }
      addSubcommand(get)
    }
    addSubcommand(chapters)
  }
  addSubcommand(episode)

  val feed = new Subcommand("feed") {
    descr("Operations on Feeds")
    shortSubcommandsHelp(true)
    val get = new Subcommand("get") with ID {
      descr("Get Feed by ID")
    }
    addSubcommand(get)

    val propose = new Subcommand("propose") {
      val url: ScallopOption[List[String]] = trailArg[List[String]](
        name = "URL [URL [...]]",
        descr = "Propose a new Feed by an URL",
        required = true)
    }
    addSubcommand(propose)
  }
  addSubcommand(feed)

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
