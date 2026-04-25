'use strict'
const merge = require('webpack-merge')
const prodEnv = require('./prod.env')
const config = require('./index')

module.exports = merge(prodEnv, {
  NODE_ENV: '"development"',
  BASE_URL: `'${config.dev.contextPath}/${config.module}/${config.views}/'`,
  API_BASE_URL: `"http://${process.env.HOST}:8056/${config.dev.contextPath}"`
})
