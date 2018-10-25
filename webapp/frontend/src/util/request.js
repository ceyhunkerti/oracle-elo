import axios from 'axios'
import store from '@/store'
import { getToken } from '@/util/auth'

// create an axios instance
const service = axios.create({
  baseURL: process.env.BASE_API, // api的base_url
  timeout: 3 * 60 * 1000 // request timeout 3 min
})

// request interceptor
service.interceptors.request.use(config => {
  // Do something before request is sent
  if (store.getters.token) {
    config.headers.Authorization = `Bearer ${getToken()}`
  }
  return config
}, error => {
  // Do something with request error
  console.log(error) // for debug
  Promise.reject(error)
})

// respone interceptor
service.interceptors.response.use(
  response => response,
  error => {
    console.log(error.response) // for debug
    if (error.request.status === 401) {
      window.getApp.$emit('APP_LOGOUT')
    }
    return Promise.reject(error)
  })

export default service
