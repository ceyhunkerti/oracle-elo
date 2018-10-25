'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')
const HOST = 'localhost'

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_API: `"http://${HOST}:9090/api/v1"`,
  TOKEN_URL: `"http://${HOST}:9090/oauth/token"`
})
