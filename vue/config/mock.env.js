'use strict'
const merge = require('webpack-merge')
const devEnv = require('./dev.env')

module.exports = merge(devEnv, {
  NODE_ENV: '"mock"',
  // API_BASE_URL: "http://127.0.0.1/:8083/fts_app"
})
