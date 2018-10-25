import api from '@/api/meta'

const state = {
}

const getters = {
}

// actions
const actions = {
  createTable({ commit }, payload) {
    return api.createTable(payload)
  }
}

// mutations
const mutations = {
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
