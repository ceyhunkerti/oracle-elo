
import _ from 'lodash'
import api from '@/api/elo'

const ADD_MAP = 'ADD_MAP'
const DELETE_MAP = 'DELETE_MAP'
const SET_MAPS = 'SET_MAPS'
const UPDATE_MAP = 'EXCLUDE_MAP'
const SET_STATUS = 'SET_STATUS'

const state = {
  dbLinks: [],
  maps: [],
  status: { error: 0, ready: 0, success: 0, running: 0, total: 0 }
}

const getters = {
  maps: state => state.maps,
  status: state => state.status
}

// actions
const actions = {
  findEloStatus({ commit }) {
    return api.findEloStatus().then(response => {
      commit(SET_STATUS, response.data)
    }, error => {
      console.log(error)
    })
  },
  findEloSize({ commit }) {
    return api.findEloSize()
  },
  findDbLinks({ commit }) {
    return api.findDbLinks()
  },
  findSourceSchemas({ commit }, { dbLink }) {
    return api.findSourceSchemas(dbLink)
  },
  findSourceTables({ commit }, { dbLink, owner }) {
    return api.findSourceTables(dbLink, owner)
  },
  findSourceColumns({ commit }, { dbLink, owner, name }) {
    return api.findSourceColumns(dbLink, owner, name)
  },
  findTargetSchemas({ commit }) {
    return api.findTargetSchemas()
  },
  findTargetTables({ commit }, { owner }) {
    return api.findTargetTables(owner)
  },
  findTargetColumns({ commit }, { owner, table }) {
    return api.findTargetColumns(owner, table)
  },
  createMap({ commit }, data) {
    return new Promise((resolve, reject) => {
      return api.createMap(data).then(response => {
        commit(ADD_MAP, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  updateMap({ commit }, data) {
    return new Promise((resolve, reject) => {
      return api.updateMap(data).then(response => {
        commit(UPDATE_MAP, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  findMap({ commit }, name) {
    return api.findMap(name)
  },
  deleteMap({ commit }, name) {
    return new Promise((resolve, reject) => {
      return api.deleteMap(name).then(response => {
        commit(DELETE_MAP, name)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  findMaps({ commit }) {
    return new Promise((resolve, reject) => {
      return api.findMaps().then(response => {
        commit(SET_MAPS, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  excludeMap({ commit }, name) {
    return new Promise((resolve, reject) => {
      return api.excludeMap(name).then(response => {
        commit(UPDATE_MAP, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  includeMap({ commit }, name) {
    return new Promise((resolve, reject) => {
      return api.includeMap(name).then(response => {
        commit(UPDATE_MAP, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  excludeMaps({ commit }, names) {
    return new Promise((resolve, reject) => {
      return api.excludeMaps(names).then(response => {
        _.each(response.data, m => commit(UPDATE_MAP, m))
        resolve(response.data)
      }, error => {
        console.log(error)
        return reject(error)
      })
    })
  },
  includeMaps({ commit }, names) {
    return new Promise((resolve, reject) => {
      return api.includeMaps(names).then(response => {
        _.each(response.data, m => commit(UPDATE_MAP, m))
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  },
  runMap({ commit }, name) {
    return api.runMap(name)
  }
}

// mutations
const mutations = {
  [ADD_MAP](state, data) {
    state.maps.push(data)
  },
  [SET_MAPS](state, data) {
    state.maps = data
  },
  [UPDATE_MAP](state, data) {
    const name = data.name
    const i = _.findIndex(state.maps, { name })
    state.maps.splice(i, 1, data)
  },
  [DELETE_MAP](state, name) {
    const i = _.findIndex(state.maps, { name })
    state.maps.splice(i, 1)
  },
  [SET_STATUS](state, data) {
    state.status = data
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
