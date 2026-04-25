import fetch from '@/utils/fetch'

export function saveDatabaseConfig(data) {
  return fetch({
    url: '/api/database/config/save',
    method: 'post',
    data
  })
}

export function getDatabaseConfigList() {
  return fetch({
    url: '/api/database/config/list',
    method: 'get'
  })
}

export function testDatabaseConnection(dbCode) {
  return fetch({
    url: `/api/database/config/${dbCode}/test`,
    method: 'post'
  })
}

export function deleteDatabaseConfig(dbCode) {
  return fetch({
    url: `/api/database/config/${dbCode}`,
    method: 'delete'
  })
}
