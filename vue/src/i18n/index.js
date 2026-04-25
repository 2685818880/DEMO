import Vue from 'vue'
import VueI18n from 'vue-i18n'
import store from '@/store'
import {getLanguage} from '@/api/common'
import enLocale from 'element-ui/lib/locale/lang/en'
import zhLocale from 'element-ui/lib/locale/lang/zh-CN'

Vue.use(VueI18n)

const i18n = new VueI18n({
  locale: store.state.locale,
  messages: {
    en: {
      ...enLocale
    },
    'zh': {
      ...zhLocale
    }
  },
  silentTranslationWarn: true
})

/* eslint-disable */
function unescapeUnicode(str) {
  // unescape unicode codes
  var codes = []
  var code = parseInt(str.substr(2), 16)
  if (code >= 0 && code < Math.pow(2, 16)) {
    codes.push(code);
  }
  // convert codes to text
  var unescaped = ''
  for (var i = 0; i < codes.length; ++i) {
    unescaped += String.fromCharCode(codes[i])
  }
  return unescaped
}

function parseData (data) {
  var res = {}
  var parameters = data.split(/\n/)
  var unicodeRE = /(\\u.{4})/ig
  for (var i = 0; i < parameters.length; i++) {
    parameters[i] = parameters[i].replace(/^\s\s*/, '').replace(/\s\s*$/, '') // trim
    if (parameters[i].length > 0 && parameters[i].match("^#") != "#") { // skip comments
      var pair = parameters[i].split('=')
      if (pair.length > 0) {
        /** Process key & value */
        var name = unescape(pair[0].replace(/\\u/g,"%u")).replace(/^\s\s*/, '').replace(/\s\s*$/, '') // trim
        var value = pair.length == 1 ? "" : pair[1]
        // process multi-line values
        while (value.match(/\\$/) === "\\") {
          value = value.substring(0, value.length - 1)
          value += parameters[++i].replace(/\s\s*$/, '') // right trim
        }
        // Put values with embedded '='s back together
        for (var s = 2; s < pair.length; s++) {
          value += '=' + pair[s]
        }
        value = value.replace(/^\s\s*/, '').replace(/\s\s*$/, '') // trim

        // handle unicode chars possibly left out
        var unicodeMatches = value.match(unicodeRE)
        if (unicodeMatches) {
          for (var u = 0; u < unicodeMatches.length; u++) {
            value = value.replace(unicodeMatches[u], unescapeUnicode(unicodeMatches[u]))
          }
        }
        // add to map
        if (value !== '') {
          res[name] = value
        }
      }
    }
  }
  return res
}

getLanguage().then((res) => {
  i18n.setLocaleMessage(store.state.locale, {
    ...parseData(res),
    ...i18n.getLocaleMessage(store.state.locale)
  })
})

export default i18n
