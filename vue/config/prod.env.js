'use strict'
const config = require('./index')

module.exports = {
  NODE_ENV: '"production"',
  BASE_URL: `'${config.dev.contextPath}/${config.module}/${config.views}/'`,
  API_BASE_URL: `"/${config.build.contextPath}"`
}
