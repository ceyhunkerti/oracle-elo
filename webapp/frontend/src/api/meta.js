import request from '@/util/request'

export default {
  createTable(payload) {
    return request.post(`/meta/create-table`, payload)
  }
}
