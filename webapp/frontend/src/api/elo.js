import request from '@/util/request'

export default {
  findEloStatus() {
    return request.get(`/elo/status`)
  },
  findEloSize() {
    return request.get(`/elo/size`)
  },
  findDbLinks() {
    return request.get(`/elo/dblinks`)
  },
  findSourceSchemas(dbLink) {
    return request.get(`/elo/source-schemas/${dbLink}`)
  },
  findSourceTables(dbLink, owner) {
    return request.get(`/elo/source-tables/${dbLink}/${owner}`)
  },
  findSourceColumns(dbLink, owner, name) {
    return request.get(`/elo/source-columns/${dbLink}/${owner}/${name}`)
  },
  findTargetSchemas() {
    return request.get(`/elo/target-schemas`)
  },
  findTargetTables(owner) {
    return request.get(`/elo/target-tables/${owner}`)
  },
  findTargetColumns(owner, table) {
    return request.get(`/elo/target-columns/${owner}/${table}`)
  },
  createMap(m) {
    return request.post(`/elo`, m)
  },
  updateMap(m) {
    return request.put(`/elo`, m)
  },
  findMap(name) {
    return request.get(`/elo/${name}`)
  },
  deleteMap(name) {
    return request.delete(`/elo/${name}`)
  },
  findMaps() {
    return request.get(`/elo`)
  },
  excludeMap(name) {
    return request.get(`/elo/exclude/${name}`)
  },
  includeMap(name) {
    return request.get(`/elo/include/${name}`)
  },
  excludeMaps(names) {
    return request.put(`/elo/exclude`, names)
  },
  includeMaps(names) {
    return request.put(`/elo/include`, names)
  },
  runMap(name) {
    return request.get(`/elo/run/${name}`)
  }

}
