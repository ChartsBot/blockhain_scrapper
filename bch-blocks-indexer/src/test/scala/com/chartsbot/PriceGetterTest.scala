package com.chartsbot

import org.json4s.DefaultFormats
import org.json4s.native.Serialization

import scala.math._
import sttp.client3._
import sttp.client3.json4s._

object PriceGetterTest {

  val backend: SttpBackend[Identity, Any] = HttpURLConnectionBackend()

  implicit val serialization: Serialization.type = org.json4s.native.Serialization
  implicit val formats: DefaultFormats.type = org.json4s.DefaultFormats

  def generateGraphqlQueryBlock(number: Int): String = {
    s"""a$number: bundle(id: 1, block: {number: $number}) {ethPrice}"""
  }

  def generateGraphQlQueriesFrom(blockFrom: Int, blockTo: Int, spacing: Int = 50): String = {
    val numberIntervals = floor((blockTo - blockFrom) / spacing).toInt
    val queries = for {
      i <- 0 to numberIntervals
    } yield {
      generateGraphqlQueryBlock(blockFrom + i * spacing)
    }
    "{" + queries.mkString(" ") + "}"
  }

  def graphqlSendQuery(url: String, query: String) = {
    val bodyGraphqlFormat = """{"operationName": "pleaseWork", "variables": {}, "query": "query """ + query + "\"}"
    println(bodyGraphqlFormat)
    val request = basicRequest.post(uri"$url")
      .body(bodyGraphqlFormat)
    request.send(backend)
  }

  def main(args: Array[String]): Unit = {
    val query = generateGraphQlQueriesFrom(10072925, 10073425)
    val url = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2"
    val response: Identity[Response[Either[String, String]]] = graphqlSendQuery(url, query)
    response.body match {
      case Left(value) =>
        println("error")
      case Right(value) =>
        println(value)
    }
  }

}
