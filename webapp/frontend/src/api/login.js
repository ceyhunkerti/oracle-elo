import request from '@/util/request'
import axios from 'axios'
// import queryString from 'query-string'

export function loginByUsername(username, password) {
  return axios.request({
    method: 'post',
    url: `${process.env.TOKEN_URL}?grant_type=password&username=${username}&password=${password}`,
    auth: {
      username: 'webapp-client-id',
      password: 'XY7kmzoNzl100'
    },
    data: {
    },
    config: {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Access-Control-Allow-Origin': '*',
        'X-Application-Context':	'application:9090'
      }
    }
  })
}

export function logout() {
  return request({
    url: '/login/logout',
    method: 'post'
  })
}

export function getUserInfo(token) {
  return request({
    url: '/users/info',
    method: 'get'
    // params: { token }
  })
}

