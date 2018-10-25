import _ from 'lodash'
import request from '@/util/request'

export default {
  findLogs(limit) {
    if (_.isNumber(limit)) {
      return request.get(`/pl/logs?limit=${limit}`)
    } else {
      return request.get(`/pl/logs`)
    }
  }
}
