
import _ from 'lodash'
import moment from 'moment'
import api from '@/api/pl'

const SET_LOGS = 'SET_MAPS'

const state = {
  logs: []
}

const getters = {
  logs: state => state.logs
}

// actions
const actions = {
  findLogs({ commit }, limit) {
    return new Promise((resolve, reject) => {
      return api.findLogs(limit).then(response => {
        commit(SET_LOGS, response.data)
        resolve(response.data)
      }, error => {
        console.log(error)
        reject(error)
      })
    })
  }
}

const timeStr = (t) => {
  return moment(t).format('MM D hh:mm:ss a')
}

const duration = (startTime, endTime) => {
  if (!startTime) return

  if (!endTime) {
    endTime = this.now
  }
  const d = moment.duration(moment(endTime) - moment(startTime))
  const h = _.padStart(d.days() * 24 + d.hours(), 2, 0)
  const m = _.padStart(d.minutes(), 2, 0)
  const s = _.padStart(d.seconds(), 2, 0)
  return `${h}:${m}:${s}`
}

// mutations
const mutations = {
  [SET_LOGS](state, data) {
    state.logs = _.map(data, d => {
      return {
        ...d,
        duration: duration(d.startTime, d.endTime),
        startTimeStr: timeStr(d.startTime),
        endTimeStr: timeStr(d.endTime)
      }
    })
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
