import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import actions from './actions'
import user from './modules/user'
import elo from './modules/elo'
import pl from './modules/pl'
import meta from './modules/meta'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    user,
    elo,
    pl,
    meta
  },
  actions,
  getters
})

export default store
