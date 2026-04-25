import * as Enums from './enums'
import { generateEnumOptions } from '@utils/helper'

/**
 * 枚举的options集合
 * @type {{}}
 */
const res = {}
Object.keys(Enums).forEach(key => {
  if (typeof Enums[key] !== 'object') {
    return
  }

  res[key] = generateEnumOptions(key)
})

export default res
