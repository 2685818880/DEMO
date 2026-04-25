import fetch from '@/utils/fetch'

/**
 * 获取语言包
 * @returns {*}
 */
function getLanguage() {
  return fetch.get('/restful/i18nRestful/language', {
    responseType: 'text'
  })
}

export {
  getLanguage
}
