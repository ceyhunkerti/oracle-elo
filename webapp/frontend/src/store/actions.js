export default {
  reset({ commit }) {

  },
  refresh({ commit }, payload) {
  },
  init() {
    this.dispatch('elo/findEloStatus')
    this.dispatch('elo/findMaps')

    setInterval(() => {
      this.dispatch('elo/findEloStatus')
    }, 1000 * 5)
  }
}
