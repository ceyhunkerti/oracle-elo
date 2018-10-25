package io.blue.controller

import javax.servlet.http.HttpServletResponse
import org.springframework.web.multipart.MultipartFile
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation._
import org.springframework.dao.DataIntegrityViolationException
import org.apache.commons.io.IOUtils
import com.fasterxml.jackson.databind.ObjectMapper

import io.blue.model.elo._
import io.blue.service.EloService

@RestController
@RequestMapping(Array("/api/v1/elo"))
class EloController @Autowired()(private val service: EloService) {

  @RequestMapping(value = Array("/dblinks"), method = Array(RequestMethod.GET))
  def findDbLinks = service.findDbLinks

  @RequestMapping(value = Array("/size"), method = Array(RequestMethod.GET))
  def findEloSize = service.findEloSize

  @RequestMapping(value = Array("/status"), method = Array(RequestMethod.GET))
  def findEloStatus = service.findEloStatus

  @RequestMapping(value = Array("/source-schemas/{dbLink}"), method = Array(RequestMethod.GET))
  def findSourceSchemas(@PathVariable("dbLink") dbLink: String) =
    service.findSourceSchemas(dbLink)

  @RequestMapping(value = Array("/source-tables/{dbLink}/{owner}"), method = Array(RequestMethod.GET))
  def findSourceTables(
    @PathVariable("dbLink") dbLink: String, @PathVariable("owner") owner: String) =
    service.findSourceTables(dbLink, owner)

  @RequestMapping(value = Array("/source-columns/{dbLink}/{owner}/{table}"), method = Array(RequestMethod.GET))
  def findSourceColumns(
    @PathVariable("dbLink") dbLink: String,
    @PathVariable("owner") owner: String,
    @PathVariable("table") table: String
  ) = service.findSourceColumns(dbLink, owner, table)

  @RequestMapping(value = Array("/target-schemas"), method = Array(RequestMethod.GET))
  def findTargetSchemas = service.findTargetSchemas

  @RequestMapping(value = Array("/target-tables/{owner}"), method = Array(RequestMethod.GET))
  def findTargetTables(@PathVariable("owner") owner: String) =
    service.findTargetTables(owner)

  @RequestMapping(value = Array("/target-columns/{owner}/{table}"), method = Array(RequestMethod.GET))
  def findTargetColumns(@PathVariable("owner") owner: String, @PathVariable("table") table: String) =
    service.findTargetColumns(owner, table)

  @RequestMapping(method = Array(RequestMethod.POST))
  def createMap(@RequestBody eloMap: EloMap) = service.createMap(eloMap)

  @RequestMapping(method = Array(RequestMethod.PUT))
  def updateMap(@RequestBody eloMap: EloMap) = service.updateMap(eloMap)

  @RequestMapping(value = Array("/{name}"), method = Array(RequestMethod.GET))
  def findMap(@PathVariable("name") name: String) = service.findMap(name)

  @RequestMapping(value = Array("/{name}"), method = Array(RequestMethod.DELETE))
  def deleteMap(@PathVariable("name") name: String) = service.deleteMap(name)

  @RequestMapping(method = Array(RequestMethod.GET))
  def findMaps = service.findMaps

  @RequestMapping(value = Array("/exclude/{name}"), method = Array(RequestMethod.GET))
  def excludeMap(@PathVariable("name") name: String) = service.excludeMap(name)

  @RequestMapping(value = Array("/include/{name}"), method = Array(RequestMethod.GET))
  def includeMap(@PathVariable("name") name: String) = service.excludeMap(name, false)

  @RequestMapping(value = Array("/exclude"), method = Array(RequestMethod.PUT))
  def excludeMaps(@RequestBody names: List[String]) = service.excludeMaps(names)

  @RequestMapping(value = Array("/include"), method = Array(RequestMethod.PUT))
  def includeMaps(@RequestBody names: List[String]) = service.excludeMaps(names, false)

  @RequestMapping(value = Array("/run/{name}"), method = Array(RequestMethod.GET))
  def run(@PathVariable("name") name: String) = service.run(name)

}